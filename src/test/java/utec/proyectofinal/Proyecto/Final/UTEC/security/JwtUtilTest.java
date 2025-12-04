package utec.proyectofinal.Proyecto.Final.UTEC.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test completo para JwtUtil
 * 
 * Cubre todas las funciones:
 * - generarToken (access token con roles y claims completos)
 * - generarRefreshToken (refresh token con claims mínimos)
 * - obtenerUserIdDelToken (extrae userId del token)
 * - esTokenValido (valida estructura, firma y expiración)
 * - obtenerUsuarioDelToken (extrae username/subject)
 * - getAccessTokenExpiration (retorna tiempo de expiración access)
 * - getRefreshTokenExpiration (retorna tiempo de expiración refresh)
 * 
 * Total de tests: 24
 */
@DisplayName("JwtUtil - Test Completo")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private Usuario usuarioTest;
    
    // Clave secreta (debe coincidir con JwtUtil)
    private final String CLAVE = "@Z9@vQ3!pL8#wX7^tR2&nG6*yM4$eB1(dF0)sH5%kJ3&uY8*rE4#wQ1@zX6^nM9$";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(CLAVE.getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // Crear usuario de prueba con todos los campos
        usuarioTest = new Usuario();
        usuarioTest.setUsuarioID(123);
        usuarioTest.setNombre("testuser");
        usuarioTest.setNombres("Juan");
        usuarioTest.setApellidos("Pérez");
        usuarioTest.setEmail("test@example.com");
        usuarioTest.setRol(Rol.ADMIN);
        usuarioTest.setEstado(EstadoUsuario.ACTIVO);
    }

    // ===== TESTS DE generarToken =====

    @Test
    @DisplayName("generarToken - Debe generar access token con rol único")
    void generarToken_rolUnico_debeGenerarTokenValido() {
        
        List<String> roles = Arrays.asList("ADMIN");

        
        String token = jwtUtil.generarToken(usuarioTest, roles);

        
        assertNotNull(token, "Token no debe ser null");
        assertFalse(token.isEmpty(), "Token no debe estar vacío");
        assertTrue(token.contains("."), "Token debe tener formato JWT (3 partes separadas por .)");
        
        // Verificar que el token es válido
        assertTrue(jwtUtil.esTokenValido(token), "Token generado debe ser válido");
    }

    @Test
    @DisplayName("generarToken - Debe incluir subject (username)")
    void generarToken_debeIncluirSubject() {
        
        List<String> roles = Arrays.asList("ADMIN");

        
        String token = jwtUtil.generarToken(usuarioTest, roles);
        String username = jwtUtil.obtenerUsuarioDelToken(token);

        
        assertEquals("testuser", username, "Subject debe ser el nombre del usuario");
    }

    @Test
    @DisplayName("generarToken - Debe incluir userId en claims")
    void generarToken_debeIncluirUserId() {
        
        List<String> roles = Arrays.asList("ADMIN");

        
        String token = jwtUtil.generarToken(usuarioTest, roles);
        Integer userId = jwtUtil.obtenerUserIdDelToken(token);

        
        assertEquals(123, userId, "userId debe coincidir con el del usuario");
    }

    @Test
    @DisplayName("generarToken - Debe incluir authorities (roles)")
    void generarToken_debeIncluirAuthorities() {
        
        List<String> roles = Arrays.asList("ADMIN", "ANALISTA");

        
        String token = jwtUtil.generarToken(usuarioTest, roles);

        
        // Parsear token para verificar authorities
        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("authorities");
        
        assertEquals(2, authorities.size(), "Debe tener 2 roles");
        assertTrue(authorities.contains("ADMIN"), "Debe contener rol ADMIN");
        assertTrue(authorities.contains("ANALISTA"), "Debe contener rol ANALISTA");
    }

    @Test
    @DisplayName("generarToken - Debe incluir claim 'type' con valor 'access'")
    void generarToken_debeIncluirTypeAccess() {
        
        List<String> roles = Arrays.asList("ADMIN");

        
        String token = jwtUtil.generarToken(usuarioTest, roles);

        
        String type = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
        
        assertEquals("access", type, "Claim 'type' debe ser 'access'");
    }

    @Test
    @DisplayName("generarToken - Debe incluir email, nombres y apellidos")
    void generarToken_debeIncluirDatosCompletos() {
        
        List<String> roles = Arrays.asList("ADMIN");

        
        String token = jwtUtil.generarToken(usuarioTest, roles);

        
        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        assertEquals("test@example.com", claims.get("email", String.class), "Email debe estar en claims");
        assertEquals("Juan", claims.get("nombres", String.class), "Nombres debe estar en claims");
        assertEquals("Pérez", claims.get("apellidos", String.class), "Apellidos debe estar en claims");
    }

    @Test
    @DisplayName("generarToken - Debe tener fecha de expiración de 1 hora")
    void generarToken_debeExpirarEn1Hora() {
        
        List<String> roles = Arrays.asList("ADMIN");
        long ahora = System.currentTimeMillis();

        
        String token = jwtUtil.generarToken(usuarioTest, roles);

        
        Date expiracion = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        
        long tiempoExpiracion = expiracion.getTime() - ahora;
        
        // Verificar que la expiración está alrededor de 1 hora (3600000 ms)
        assertTrue(tiempoExpiracion > 3590000 && tiempoExpiracion <= 3600000, 
                   "Token debe expirar en aproximadamente 1 hora");
    }

    // ===== TESTS DE generarRefreshToken =====

    @Test
    @DisplayName("generarRefreshToken - Debe generar refresh token válido")
    void generarRefreshToken_debeGenerarTokenValido() {
        
        String refreshToken = jwtUtil.generarRefreshToken(usuarioTest);

        
        assertNotNull(refreshToken, "Refresh token no debe ser null");
        assertFalse(refreshToken.isEmpty(), "Refresh token no debe estar vacío");
        assertTrue(jwtUtil.esTokenValido(refreshToken), "Refresh token debe ser válido");
    }

    @Test
    @DisplayName("generarRefreshToken - Debe incluir subject (username)")
    void generarRefreshToken_debeIncluirSubject() {
        
        String refreshToken = jwtUtil.generarRefreshToken(usuarioTest);
        String username = jwtUtil.obtenerUsuarioDelToken(refreshToken);

        
        assertEquals("testuser", username, "Subject debe ser el nombre del usuario");
    }

    @Test
    @DisplayName("generarRefreshToken - Debe incluir userId")
    void generarRefreshToken_debeIncluirUserId() {
        
        String refreshToken = jwtUtil.generarRefreshToken(usuarioTest);
        Integer userId = jwtUtil.obtenerUserIdDelToken(refreshToken);

        
        assertEquals(123, userId, "userId debe coincidir con el del usuario");
    }

    @Test
    @DisplayName("generarRefreshToken - Debe incluir claim 'type' con valor 'refresh'")
    void generarRefreshToken_debeIncluirTypeRefresh() {
        
        String refreshToken = jwtUtil.generarRefreshToken(usuarioTest);

        
        String type = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .get("type", String.class);
        
        assertEquals("refresh", type, "Claim 'type' debe ser 'refresh'");
    }

    @Test
    @DisplayName("generarRefreshToken - NO debe incluir authorities")
    void generarRefreshToken_noDebeIncluirAuthorities() {
        
        String refreshToken = jwtUtil.generarRefreshToken(usuarioTest);

        
        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();
        
        assertNull(claims.get("authorities"), "Refresh token NO debe contener authorities");
    }

    @Test
    @DisplayName("generarRefreshToken - Debe tener fecha de expiración de 7 días")
    void generarRefreshToken_debeExpirarEn7Dias() {
        
        long ahora = System.currentTimeMillis();

        
        String refreshToken = jwtUtil.generarRefreshToken(usuarioTest);

        
        Date expiracion = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getExpiration();
        
        long tiempoExpiracion = expiracion.getTime() - ahora;
        long siete_dias = 604800000; // 7 días en ms
        
        // Verificar que la expiración está alrededor de 7 días
        assertTrue(tiempoExpiracion > (siete_dias - 10000) && tiempoExpiracion <= siete_dias, 
                   "Refresh token debe expirar en aproximadamente 7 días");
    }

    // ===== TESTS DE obtenerUserIdDelToken =====

    @Test
    @DisplayName("obtenerUserIdDelToken - Debe extraer userId de access token")
    void obtenerUserIdDelToken_accessToken_debeExtraerUserId() {
        
        String token = jwtUtil.generarToken(usuarioTest, Arrays.asList("ADMIN"));

        
        Integer userId = jwtUtil.obtenerUserIdDelToken(token);

        
        assertNotNull(userId, "userId no debe ser null");
        assertEquals(123, userId, "userId debe ser 123");
    }

    @Test
    @DisplayName("obtenerUserIdDelToken - Debe extraer userId de refresh token")
    void obtenerUserIdDelToken_refreshToken_debeExtraerUserId() {
        
        String refreshToken = jwtUtil.generarRefreshToken(usuarioTest);

        
        Integer userId = jwtUtil.obtenerUserIdDelToken(refreshToken);

        
        assertNotNull(userId, "userId no debe ser null");
        assertEquals(123, userId, "userId debe ser 123");
    }

    @Test
    @DisplayName("obtenerUserIdDelToken - Debe lanzar excepción con token inválido")
    void obtenerUserIdDelToken_tokenInvalido_debeLanzarExcepcion() {
        
        String tokenInvalido = "token.invalido.xyz";

        
        assertThrows(Exception.class, () -> {
            jwtUtil.obtenerUserIdDelToken(tokenInvalido);
        }, "Debe lanzar excepción con token inválido");
    }

    // ===== TESTS DE esTokenValido =====

    @Test
    @DisplayName("esTokenValido - Token válido debe retornar true")
    void esTokenValido_tokenValido_debeRetornarTrue() {
        
        String token = jwtUtil.generarToken(usuarioTest, Arrays.asList("ADMIN"));

        
        boolean esValido = jwtUtil.esTokenValido(token);

        
        assertTrue(esValido, "Token válido debe retornar true");
    }

    @Test
    @DisplayName("esTokenValido - Token expirado debe retornar false")
    void esTokenValido_tokenExpirado_debeRetornarFalse() {
        
        String tokenExpirado = Jwts.builder()
                .subject("testuser")
                .claim("userId", 123)
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();

        
        boolean esValido = jwtUtil.esTokenValido(tokenExpirado);

        
        assertFalse(esValido, "Token expirado debe retornar false");
    }

    @Test
    @DisplayName("esTokenValido - Token con firma incorrecta debe retornar false")
    void esTokenValido_firmaIncorrecta_debeRetornarFalse() {
        
        SecretKey otraLlave = Keys.hmacShaKeyFor("otra_clave_secreta_diferente_muy_larga_123456789".getBytes(StandardCharsets.UTF_8));
        String tokenConOtraFirma = Jwts.builder()
                .subject("hacker")
                .claim("userId", 999)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(otraLlave)
                .compact();

        
        boolean esValido = jwtUtil.esTokenValido(tokenConOtraFirma);

        
        assertFalse(esValido, "Token con firma incorrecta debe retornar false");
    }

    @Test
    @DisplayName("esTokenValido - Token malformado debe retornar false")
    void esTokenValido_tokenMalformado_debeRetornarFalse() {
        
        String tokenMalformado = "token.malformado.xyz";

        
        boolean esValido = jwtUtil.esTokenValido(tokenMalformado);

        
        assertFalse(esValido, "Token malformado debe retornar false");
    }

    @Test
    @DisplayName("esTokenValido - Token vacío debe retornar false")
    void esTokenValido_tokenVacio_debeRetornarFalse() {
        
        String tokenVacio = "";

        
        boolean esValido = jwtUtil.esTokenValido(tokenVacio);

        
        assertFalse(esValido, "Token vacío debe retornar false");
    }

    @Test
    @DisplayName("esTokenValido - Token null debe retornar false")
    void esTokenValido_tokenNull_debeRetornarFalse() {
        
        boolean esValido = jwtUtil.esTokenValido(null);

        
        assertFalse(esValido, "Token null debe retornar false");
    }

    // ===== TESTS DE obtenerUsuarioDelToken =====

    @Test
    @DisplayName("obtenerUsuarioDelToken - Debe extraer username de access token")
    void obtenerUsuarioDelToken_accessToken_debeExtraerUsername() {
        
        String token = jwtUtil.generarToken(usuarioTest, Arrays.asList("ADMIN"));

        
        String username = jwtUtil.obtenerUsuarioDelToken(token);

        
        assertEquals("testuser", username, "Username debe ser 'testuser'");
    }

    @Test
    @DisplayName("obtenerUsuarioDelToken - Debe extraer username de refresh token")
    void obtenerUsuarioDelToken_refreshToken_debeExtraerUsername() {
        
        String refreshToken = jwtUtil.generarRefreshToken(usuarioTest);

        
        String username = jwtUtil.obtenerUsuarioDelToken(refreshToken);

        
        assertEquals("testuser", username, "Username debe ser 'testuser'");
    }

    @Test
    @DisplayName("obtenerUsuarioDelToken - Debe lanzar excepción con token inválido")
    void obtenerUsuarioDelToken_tokenInvalido_debeLanzarExcepcion() {
        
        String tokenInvalido = "token.invalido.xyz";

        
        assertThrows(Exception.class, () -> {
            jwtUtil.obtenerUsuarioDelToken(tokenInvalido);
        }, "Debe lanzar excepción con token inválido");
    }

    // ===== TESTS DE getAccessTokenExpiration =====

    @Test
    @DisplayName("getAccessTokenExpiration - Debe retornar 1 hora en milisegundos")
    void getAccessTokenExpiration_debeRetornar1Hora() {
        
        long expiracion = jwtUtil.getAccessTokenExpiration();

        
        assertEquals(3600000, expiracion, "Access token expiration debe ser 3600000 ms (1 hora)");
    }

    // ===== TESTS DE getRefreshTokenExpiration =====

    @Test
    @DisplayName("getRefreshTokenExpiration - Debe retornar 7 días en milisegundos")
    void getRefreshTokenExpiration_debeRetornar7Dias() {
        
        long expiracion = jwtUtil.getRefreshTokenExpiration();

        
        assertEquals(604800000, expiracion, "Refresh token expiration debe ser 604800000 ms (7 días)");
    }
}
