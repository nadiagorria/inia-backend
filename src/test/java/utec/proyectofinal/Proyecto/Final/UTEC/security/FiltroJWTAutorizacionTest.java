package utec.proyectofinal.Proyecto.Final.UTEC.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test completo para FiltroJWTAutorizacion
 * 
 * Cubre todas las funciones:
 * - extraerToken (desde cookies y header Authorization)
 * - doFilterInternal (flujo completo del filtro)
 * - crearAutenticacion (creación del contexto de seguridad)
 * - validarToken (validación de JWT)
 * 
 * Total de tests: 18
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FiltroJWTAutorizacion - Test Completo")
class FiltroJWTAutorizacionTest {

    private FiltroJWTAutorizacion filtro;
    
    @Mock
    private FilterChain filterChain;
    
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    // Clave secreta (debe coincidir con la del filtro)
    private final String CLAVE = "@Z9@vQ3!pL8#wX7^tR2&nG6*yM4$eB1(dF0)sH5%kJ3&uY8*rE4#wQ1@zX6^nM9$";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(CLAVE.getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        filtro = new FiltroJWTAutorizacion();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext(); // Limpiar contexto antes de cada test
    }

    // ===== TESTS DE extraerToken =====

    @Test
    @DisplayName("extraerToken - Debe extraer token desde cookie accessToken")
    void extraerToken_desdeCookie_debeRetornarToken() throws ServletException, IOException {
        
        String tokenEsperado = generarTokenValido("testuser", List.of("ADMIN"));
        Cookie cookie = new Cookie("accessToken", tokenEsperado);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Debe crear autenticación");
        assertEquals("testuser", auth.getName());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("extraerToken - Debe extraer token desde header Authorization Bearer")
    void extraerToken_desdeHeaderBearer_debeRetornarToken() throws ServletException, IOException {
        
        String tokenEsperado = generarTokenValido("analista", List.of("ANALISTA"));
        request.addHeader("Authorization", "Bearer " + tokenEsperado);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Debe crear autenticación desde header");
        assertEquals("analista", auth.getName());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ANALISTA")));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("extraerToken - Cookie tiene prioridad sobre header Authorization")
    void extraerToken_cookieTienePrioridad_debeUsarCookie() throws ServletException, IOException {
        
        String tokenCookie = generarTokenValido("usuario_cookie", List.of("ADMIN"));
        String tokenHeader = generarTokenValido("usuario_header", List.of("OBSERVADOR"));
        
        Cookie cookie = new Cookie("accessToken", tokenCookie);
        request.setCookies(cookie);
        request.addHeader("Authorization", "Bearer " + tokenHeader);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("usuario_cookie", auth.getName(), "Debe usar el token de la cookie");
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("extraerToken - Sin token debe limpiar contexto de seguridad")
    void extraerToken_sinToken_debeLimpiarContexto() throws ServletException, IOException {
        

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No debe crear autenticación sin token");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("extraerToken - Header sin prefijo Bearer debe ignorarse")
    void extraerToken_headerSinBearer_debeIgnorarse() throws ServletException, IOException {
        
        String token = generarTokenValido("user", List.of("ADMIN"));
        request.addHeader("Authorization", token); 

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No debe autenticar sin prefijo Bearer");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("extraerToken - Cookie con nombre incorrecto debe ignorarse")
    void extraerToken_cookieNombreIncorrecto_debeIgnorarse() throws ServletException, IOException {
        
        String token = generarTokenValido("user", List.of("ADMIN"));
        Cookie cookieIncorrecta = new Cookie("otherCookie", token);
        request.setCookies(cookieIncorrecta);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No debe autenticar con cookie incorrecta");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ===== TESTS DE doFilterInternal =====

    @Test
    @DisplayName("doFilterInternal - Token válido debe crear autenticación y continuar filtro")
    void doFilterInternal_tokenValido_debeCrearAutenticacionYContinuar() throws ServletException, IOException {
        
        String token = generarTokenValido("admin", List.of("ADMIN", "ANALISTA"));
        Cookie cookie = new Cookie("accessToken", token);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Debe crear autenticación");
        assertEquals("admin", auth.getName());
        assertEquals(2, auth.getAuthorities().size());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Token expirado debe limpiar contexto y continuar")
    void doFilterInternal_tokenExpirado_debeLimpiarContextoYContinuar() throws ServletException, IOException {
        
        String tokenExpirado = generarTokenExpirado("user", List.of("ADMIN"));
        Cookie cookie = new Cookie("accessToken", tokenExpirado);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Debe limpiar contexto con token expirado");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Token malformado debe limpiar contexto y continuar")
    void doFilterInternal_tokenMalformado_debeLimpiarContextoYContinuar() throws ServletException, IOException {
        
        String tokenMalformado = "token.invalido.malformado";
        Cookie cookie = new Cookie("accessToken", tokenMalformado);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Debe limpiar contexto con token malformado");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Token sin authorities debe limpiar contexto")
    void doFilterInternal_tokenSinAuthorities_debeLimpiarContexto() throws ServletException, IOException {
        
        String tokenSinAuthorities = generarTokenSinAuthorities("user");
        Cookie cookie = new Cookie("accessToken", tokenSinAuthorities);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No debe crear autenticación sin authorities");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Token vacío debe limpiar contexto")
    void doFilterInternal_tokenVacio_debeLimpiarContexto() throws ServletException, IOException {
        
        Cookie cookie = new Cookie("accessToken", "");
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No debe crear autenticación con token vacío");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ===== TESTS DE crearAutenticacion =====

    @Test
    @DisplayName("crearAutenticacion - Debe crear autenticación con rol único")
    void crearAutenticacion_rolUnico_debeCrearAutenticacion() throws ServletException, IOException {
        
        String token = generarTokenValido("observador", List.of("OBSERVADOR"));
        Cookie cookie = new Cookie("accessToken", token);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("observador", auth.getName());
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OBSERVADOR")));
    }

    @Test
    @DisplayName("crearAutenticacion - Debe agregar prefijo ROLE_ a todas las authorities")
    void crearAutenticacion_multipleRoles_debeAgregarPrefijoRole() throws ServletException, IOException {
        
        String token = generarTokenValido("superuser", List.of("ADMIN", "ANALISTA", "OBSERVADOR"));
        Cookie cookie = new Cookie("accessToken", token);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(3, auth.getAuthorities().size());
        
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ANALISTA")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_OBSERVADOR")));
    }

    @Test
    @DisplayName("crearAutenticacion - Debe establecer autenticación en SecurityContext")
    void crearAutenticacion_debeEstablecerEnSecurityContext() throws ServletException, IOException {
        
        String token = generarTokenValido("testuser", List.of("ADMIN"));
        Cookie cookie = new Cookie("accessToken", token);
        request.setCookies(cookie);
        
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Contexto debe estar vacío inicialmente");

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Debe establecer autenticación en SecurityContext");
        assertEquals("testuser", auth.getPrincipal());
        assertNull(auth.getCredentials(), "Credentials debe ser null");
    }

    // ===== TESTS DE validarToken =====

    @Test
    @DisplayName("validarToken - Token válido debe retornar claims correctos")
    void validarToken_tokenValido_debeRetornarClaims() throws ServletException, IOException {
        
        String username = "validuser";
        List<String> roles = List.of("ADMIN", "ANALISTA");
        String token = generarTokenValido(username, roles);
        Cookie cookie = new Cookie("accessToken", token);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(username, auth.getName());
        assertEquals(roles.size(), auth.getAuthorities().size());
    }

    @Test
    @DisplayName("validarToken - Token con firma incorrecta debe lanzar excepción")
    void validarToken_firmaIncorrecta_debeLanzarExcepcion() throws ServletException, IOException {
        
        SecretKey otraLlave = Keys.hmacShaKeyFor("otra_clave_secreta_diferente_muy_larga_123456789".getBytes(StandardCharsets.UTF_8));
        String tokenConOtraFirma = Jwts.builder()
                .subject("hacker")
                .claim("authorities", List.of("ADMIN"))
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(otraLlave)
                .compact();
        
        Cookie cookie = new Cookie("accessToken", tokenConOtraFirma);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No debe autenticar con firma incorrecta");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("validarToken - Token expirado debe lanzar ExpiredJwtException")
    void validarToken_tokenExpirado_debeLanzarExpiredJwtException() throws ServletException, IOException {
        
        String tokenExpirado = generarTokenExpirado("expireduser", List.of("ADMIN"));
        Cookie cookie = new Cookie("accessToken", tokenExpirado);
        request.setCookies(cookie);

        
        filtro.doFilterInternal(request, response, filterChain);

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No debe autenticar con token expirado");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Genera un token JWT válido con el username y roles proporcionados
     */
    private String generarTokenValido(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("authorities", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Genera un token JWT expirado
     */
    private String generarTokenExpirado(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("authorities", roles)
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Genera un token JWT sin el claim 'authorities'
     */
    private String generarTokenSinAuthorities(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }
}
