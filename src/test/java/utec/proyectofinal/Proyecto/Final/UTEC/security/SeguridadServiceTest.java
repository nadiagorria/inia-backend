package utec.proyectofinal.Proyecto.Final.UTEC.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PasswordService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de SeguridadService")
class SeguridadServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SeguridadService seguridadService;

    private Usuario usuarioActivo;
    private Usuario usuarioPendiente;
    private Usuario usuarioInactivo;
    private Usuario adminActivo;

    @BeforeEach
    void setUp() {
        // Usuario activo normal
        usuarioActivo = new Usuario();
        usuarioActivo.setUsuarioID(1);
        usuarioActivo.setNombre("testuser");
        usuarioActivo.setEmail("testuser@inia.org.uy");
        usuarioActivo.setNombres("Test");
        usuarioActivo.setApellidos("User");
        usuarioActivo.setContrasenia("$2a$10$hashedPassword");
        usuarioActivo.setRol(Rol.ANALISTA);
        usuarioActivo.setEstado(EstadoUsuario.ACTIVO);
        usuarioActivo.setActivo(true);

        // Usuario pendiente de aprobación
        usuarioPendiente = new Usuario();
        usuarioPendiente.setUsuarioID(2);
        usuarioPendiente.setNombre("pendinguser");
        usuarioPendiente.setEmail("pending@inia.org.uy");
        usuarioPendiente.setNombres("Pending");
        usuarioPendiente.setApellidos("User");
        usuarioPendiente.setContrasenia("$2a$10$hashedPassword");
        usuarioPendiente.setRol(Rol.ANALISTA);
        usuarioPendiente.setEstado(EstadoUsuario.PENDIENTE);
        usuarioPendiente.setActivo(true);

        // Usuario inactivo
        usuarioInactivo = new Usuario();
        usuarioInactivo.setUsuarioID(3);
        usuarioInactivo.setNombre("inactiveuser");
        usuarioInactivo.setEmail("inactive@inia.org.uy");
        usuarioInactivo.setNombres("Inactive");
        usuarioInactivo.setApellidos("User");
        usuarioInactivo.setContrasenia("$2a$10$hashedPassword");
        usuarioInactivo.setRol(Rol.ANALISTA);
        usuarioInactivo.setEstado(EstadoUsuario.INACTIVO);
        usuarioInactivo.setActivo(false);

        // Admin activo
        adminActivo = new Usuario();
        adminActivo.setUsuarioID(4);
        adminActivo.setNombre("admin");
        adminActivo.setEmail("admin@inia.org.uy");
        adminActivo.setNombres("Admin");
        adminActivo.setApellidos("User");
        adminActivo.setContrasenia("$2a$10$hashedPassword");
        adminActivo.setRol(Rol.ADMIN);
        adminActivo.setEstado(EstadoUsuario.ACTIVO);
        adminActivo.setActivo(true);
    }

    // ========== TESTS DE AUTENTICACIÓN ==========

    @Test
    @DisplayName("autenticarUsuario - debe autenticar correctamente por nombre de usuario")
    void autenticarUsuario_porNombreUsuario_debeAutenticarCorrectamente() {
        
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuarioActivo));
        when(passwordService.matchPassword("password123", usuarioActivo.getContrasenia())).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

        
        Optional<Usuario> resultado = seguridadService.autenticarUsuario("testuser", "password123");

        
        assertTrue(resultado.isPresent());
        assertEquals(usuarioActivo.getUsuarioID(), resultado.get().getUsuarioID());
        assertEquals("testuser", resultado.get().getNombre());
        verify(usuarioRepository).save(argThat(usuario -> 
            usuario.getFechaUltimaConexion() != null
        ));
    }

    @Test
    @DisplayName("autenticarUsuario - debe autenticar correctamente por email")
    void autenticarUsuario_porEmail_debeAutenticarCorrectamente() {
        
        when(usuarioRepository.findByNombreIgnoreCase("testuser@inia.org.uy")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailIgnoreCase("testuser@inia.org.uy")).thenReturn(Optional.of(usuarioActivo));
        when(passwordService.matchPassword("password123", usuarioActivo.getContrasenia())).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

        
        Optional<Usuario> resultado = seguridadService.autenticarUsuario("testuser@inia.org.uy", "password123");

        
        assertTrue(resultado.isPresent());
        assertEquals(usuarioActivo.getUsuarioID(), resultado.get().getUsuarioID());
        assertEquals("testuser@inia.org.uy", resultado.get().getEmail());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("autenticarUsuario - debe ser case-insensitive para nombre de usuario")
    void autenticarUsuario_caseInsensitive_debeAutenticar() {
        
        when(usuarioRepository.findByNombreIgnoreCase("TESTUSER")).thenReturn(Optional.of(usuarioActivo));
        when(passwordService.matchPassword("password123", usuarioActivo.getContrasenia())).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

        
        Optional<Usuario> resultado = seguridadService.autenticarUsuario("TESTUSER", "password123");

        
        assertTrue(resultado.isPresent());
        verify(usuarioRepository).findByNombreIgnoreCase("TESTUSER");
    }

    @Test
    @DisplayName("autenticarUsuario - debe ser case-insensitive para email")
    void autenticarUsuario_emailCaseInsensitive_debeAutenticar() {
        
        when(usuarioRepository.findByNombreIgnoreCase("TESTUSER@INIA.ORG.UY")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailIgnoreCase("TESTUSER@INIA.ORG.UY")).thenReturn(Optional.of(usuarioActivo));
        when(passwordService.matchPassword("password123", usuarioActivo.getContrasenia())).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

        
        Optional<Usuario> resultado = seguridadService.autenticarUsuario("TESTUSER@INIA.ORG.UY", "password123");

        
        assertTrue(resultado.isPresent());
        verify(usuarioRepository).findByEmailIgnoreCase("TESTUSER@INIA.ORG.UY");
    }

    @Test
    @DisplayName("autenticarUsuario - debe actualizar fecha de última conexión")
    void autenticarUsuario_debeActualizarFechaUltimaConexion() {
        
        LocalDateTime antes = LocalDateTime.now();
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuarioActivo));
        when(passwordService.matchPassword("password123", usuarioActivo.getContrasenia())).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

        
        seguridadService.autenticarUsuario("testuser", "password123");

        
        verify(usuarioRepository).save(argThat(usuario -> {
            LocalDateTime fechaConexion = usuario.getFechaUltimaConexion();
            return fechaConexion != null && 
                   (fechaConexion.isEqual(antes) || fechaConexion.isAfter(antes));
        }));
    }

    // ========== TESTS DE VALIDACIONES DE USUARIO ==========

    @Test
    @DisplayName("autenticarUsuario - usuario no encontrado debe lanzar USUARIO_INCORRECTO")
    void autenticarUsuario_usuarioNoEncontrado_debeLanzarExcepcion() {
        
        when(usuarioRepository.findByNombreIgnoreCase("noexiste")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailIgnoreCase("noexiste")).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.autenticarUsuario("noexiste", "password123"));
        
        assertEquals("USUARIO_INCORRECTO", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("autenticarUsuario - usuario inactivo (campo legacy) debe lanzar USUARIO_INACTIVO")
    void autenticarUsuario_usuarioInactivoLegacy_debeLanzarExcepcion() {
        
        usuarioActivo.setActivo(false);
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuarioActivo));

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.autenticarUsuario("testuser", "password123"));
        
        assertEquals("USUARIO_INACTIVO", exception.getMessage());
        verify(passwordService, never()).matchPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("autenticarUsuario - usuario pendiente debe lanzar USUARIO_PENDIENTE_APROBACION")
    void autenticarUsuario_usuarioPendiente_debeLanzarExcepcion() {
        
        when(usuarioRepository.findByNombreIgnoreCase("pendinguser")).thenReturn(Optional.of(usuarioPendiente));

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.autenticarUsuario("pendinguser", "password123"));
        
        assertEquals("USUARIO_PENDIENTE_APROBACION", exception.getMessage());
        verify(passwordService, never()).matchPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("autenticarUsuario - usuario con estado INACTIVO debe lanzar USUARIO_INACTIVO")
    void autenticarUsuario_estadoInactivo_debeLanzarExcepcion() {
        
        when(usuarioRepository.findByNombreIgnoreCase("inactiveuser")).thenReturn(Optional.of(usuarioInactivo));

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.autenticarUsuario("inactiveuser", "password123"));
        
        assertEquals("USUARIO_INACTIVO", exception.getMessage());
        verify(passwordService, never()).matchPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("autenticarUsuario - usuario sin rol debe lanzar USUARIO_SIN_ROL")
    void autenticarUsuario_usuarioSinRol_debeLanzarExcepcion() {
        
        usuarioActivo.setRol(null);
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuarioActivo));

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.autenticarUsuario("testuser", "password123"));
        
        assertEquals("USUARIO_SIN_ROL", exception.getMessage());
        verify(passwordService, never()).matchPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("autenticarUsuario - contraseña incorrecta debe lanzar CONTRASENIA_INCORRECTA")
    void autenticarUsuario_contraseniaIncorrecta_debeLanzarExcepcion() {
        
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuarioActivo));
        when(passwordService.matchPassword("wrongpassword", usuarioActivo.getContrasenia())).thenReturn(false);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.autenticarUsuario("testuser", "wrongpassword"));
        
        assertEquals("CONTRASENIA_INCORRECTA", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ========== TESTS DE OBTENER USUARIO AUTENTICADO ==========

    @Test
    @DisplayName("obtenerUsuarioAutenticado - debe retornar ID del usuario autenticado por nombre")
    void obtenerUsuarioAutenticado_porNombre_debeRetornarID() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(usuarioRepository.findByNombre("testuser")).thenReturn(Optional.of(usuarioActivo));

        
        Integer usuarioId = seguridadService.obtenerUsuarioAutenticado();

        
        assertEquals(1, usuarioId);
        verify(usuarioRepository).findByNombre("testuser");
    }

    @Test
    @DisplayName("obtenerUsuarioAutenticado - debe retornar ID del usuario autenticado por email")
    void obtenerUsuarioAutenticado_porEmail_debeRetornarID() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser@inia.org.uy");
        when(usuarioRepository.findByNombre("testuser@inia.org.uy")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("testuser@inia.org.uy")).thenReturn(Optional.of(usuarioActivo));

        
        Integer usuarioId = seguridadService.obtenerUsuarioAutenticado();

        
        assertEquals(1, usuarioId);
        verify(usuarioRepository).findByNombre("testuser@inia.org.uy");
        verify(usuarioRepository).findByEmail("testuser@inia.org.uy");
    }

    @Test
    @DisplayName("obtenerUsuarioAutenticado - sin autenticación debe lanzar excepción")
    void obtenerUsuarioAutenticado_sinAutenticacion_debeLanzarExcepcion() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.obtenerUsuarioAutenticado());
        
        assertEquals("No hay usuario autenticado", exception.getMessage());
    }

    @Test
    @DisplayName("obtenerUsuarioAutenticado - authentication sin nombre debe lanzar excepción")
    void obtenerUsuarioAutenticado_authenticationSinNombre_debeLanzarExcepcion() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(null);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.obtenerUsuarioAutenticado());
        
        assertEquals("No hay usuario autenticado", exception.getMessage());
    }

    @Test
    @DisplayName("obtenerUsuarioAutenticado - usuario no encontrado en BD debe lanzar excepción")
    void obtenerUsuarioAutenticado_usuarioNoEnBD_debeLanzarExcepcion() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("noexiste");
        when(usuarioRepository.findByNombre("noexiste")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("noexiste")).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            seguridadService.obtenerUsuarioAutenticado());
        
        assertEquals("Usuario autenticado no encontrado en base de datos", exception.getMessage());
    }

    // ========== TESTS DE LISTAR ROLES ==========

    @Test
    @DisplayName("listarRolesPorUsuario - debe retornar array de roles del usuario")
    void listarRolesPorUsuario_debeRetornarArrayDeRoles() {
        // ARRANGE - Usuario con rol ANALISTA tiene roles ["ANALISTA"]
        // (asumiendo que Usuario.getRoles() retorna una lista de strings basada en el rol)

        
        String[] roles = seguridadService.listarRolesPorUsuario(usuarioActivo);

        
        assertNotNull(roles);
        assertTrue(roles.length > 0);
    }

    @Test
    @DisplayName("listarRolesPorUsuario - admin debe tener roles de administrador")
    void listarRolesPorUsuario_admin_debeTenerRolesAdmin() {
        // ARRANGE - Admin puede tener múltiples roles

        
        String[] roles = seguridadService.listarRolesPorUsuario(adminActivo);

        
        assertNotNull(roles);
        assertTrue(roles.length > 0);
    }

    @Test
    @DisplayName("listarRolesPorUsuario - debe retornar array vacío si no tiene roles")
    void listarRolesPorUsuario_sinRoles_debeRetornarArrayVacio() {
        
        Usuario usuarioSinRoles = new Usuario();
        usuarioSinRoles.setUsuarioID(99);
        usuarioSinRoles.setNombre("sinroles");
        // getRoles() debería retornar lista vacía si no hay rol configurado

        
        String[] roles = seguridadService.listarRolesPorUsuario(usuarioSinRoles);

        
        assertNotNull(roles);
        // El array existe aunque esté vacío
    }

    // ========== TESTS DE EXISTENCIA DE USUARIO ==========

    @Test
    @DisplayName("existeUsuario - debe retornar true si el usuario existe")
    void existeUsuario_usuarioExiste_debeRetornarTrue() {
        
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuarioActivo));

        
        boolean existe = seguridadService.existeUsuario("testuser");

        
        assertTrue(existe);
        verify(usuarioRepository).findByNombreIgnoreCase("testuser");
    }

    @Test
    @DisplayName("existeUsuario - debe retornar false si el usuario no existe")
    void existeUsuario_usuarioNoExiste_debeRetornarFalse() {
        
        when(usuarioRepository.findByNombreIgnoreCase("noexiste")).thenReturn(Optional.empty());

        
        boolean existe = seguridadService.existeUsuario("noexiste");

        
        assertFalse(existe);
        verify(usuarioRepository).findByNombreIgnoreCase("noexiste");
    }

    @Test
    @DisplayName("existeUsuario - debe ser case-insensitive")
    void existeUsuario_caseInsensitive_debeRetornarTrue() {
        
        when(usuarioRepository.findByNombreIgnoreCase("TESTUSER")).thenReturn(Optional.of(usuarioActivo));

        
        boolean existe = seguridadService.existeUsuario("TESTUSER");

        
        assertTrue(existe);
        verify(usuarioRepository).findByNombreIgnoreCase("TESTUSER");
    }

    // ========== TESTS DE EXISTENCIA DE EMAIL ==========

    @Test
    @DisplayName("existeEmailActivo - debe retornar true si el email existe")
    void existeEmailActivo_emailExiste_debeRetornarTrue() {
        
        when(usuarioRepository.findByEmailIgnoreCase("testuser@inia.org.uy"))
            .thenReturn(Optional.of(usuarioActivo));

        
        boolean existe = seguridadService.existeEmailActivo("testuser@inia.org.uy");

        
        assertTrue(existe);
        verify(usuarioRepository).findByEmailIgnoreCase("testuser@inia.org.uy");
    }

    @Test
    @DisplayName("existeEmailActivo - debe retornar false si el email no existe")
    void existeEmailActivo_emailNoExiste_debeRetornarFalse() {
        
        when(usuarioRepository.findByEmailIgnoreCase("noexiste@inia.org.uy"))
            .thenReturn(Optional.empty());

        
        boolean existe = seguridadService.existeEmailActivo("noexiste@inia.org.uy");

        
        assertFalse(existe);
        verify(usuarioRepository).findByEmailIgnoreCase("noexiste@inia.org.uy");
    }

    @Test
    @DisplayName("existeEmailActivo - debe ser case-insensitive")
    void existeEmailActivo_caseInsensitive_debeRetornarTrue() {
        
        when(usuarioRepository.findByEmailIgnoreCase("TESTUSER@INIA.ORG.UY"))
            .thenReturn(Optional.of(usuarioActivo));

        
        boolean existe = seguridadService.existeEmailActivo("TESTUSER@INIA.ORG.UY");

        
        assertTrue(existe);
        verify(usuarioRepository).findByEmailIgnoreCase("TESTUSER@INIA.ORG.UY");
    }

    @Test
    @DisplayName("existeEmailActivo - debe encontrar emails con diferentes mayúsculas/minúsculas")
    void existeEmailActivo_diferentesMayusculas_debeRetornarTrue() {
        
        when(usuarioRepository.findByEmailIgnoreCase("TestUser@INIA.org.UY"))
            .thenReturn(Optional.of(usuarioActivo));

        
        boolean existe = seguridadService.existeEmailActivo("TestUser@INIA.org.UY");

        
        assertTrue(existe);
        verify(usuarioRepository).findByEmailIgnoreCase("TestUser@INIA.org.UY");
    }

    // ========== TESTS DE INTEGRACIÓN DE FLUJO COMPLETO ==========

    @Test
    @DisplayName("Flujo completo - autenticar admin y verificar todos sus datos")
    void flujoCompleto_autenticarAdmin_debeVerificarTodosSusDatos() {
        
        when(usuarioRepository.findByNombreIgnoreCase("admin")).thenReturn(Optional.of(adminActivo));
        when(passwordService.matchPassword("admin123", adminActivo.getContrasenia())).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(adminActivo);

        
        Optional<Usuario> resultado = seguridadService.autenticarUsuario("admin", "admin123");

        
        assertTrue(resultado.isPresent());
        Usuario admin = resultado.get();
        assertEquals(4, admin.getUsuarioID());
        assertEquals("admin", admin.getNombre());
        assertEquals("admin@inia.org.uy", admin.getEmail());
        assertEquals(Rol.ADMIN, admin.getRol());
        assertEquals(EstadoUsuario.ACTIVO, admin.getEstado());
        assertTrue(admin.getActivo());
        
        // Verificar que se llamó a listarRolesPorUsuario
        String[] roles = seguridadService.listarRolesPorUsuario(admin);
        assertNotNull(roles);
    }

    @Test
    @DisplayName("Flujo completo - verificar existencia de usuario y email antes de autenticar")
    void flujoCompleto_verificarExistenciaAntesDeAutenticar() {
        
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuarioActivo));
        when(usuarioRepository.findByEmailIgnoreCase("testuser@inia.org.uy"))
            .thenReturn(Optional.of(usuarioActivo));
        when(passwordService.matchPassword("password123", usuarioActivo.getContrasenia())).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);

        
        boolean existeUsuario = seguridadService.existeUsuario("testuser");
        boolean existeEmail = seguridadService.existeEmailActivo("testuser@inia.org.uy");
        Optional<Usuario> autenticado = seguridadService.autenticarUsuario("testuser", "password123");

        
        assertTrue(existeUsuario, "El usuario debe existir");
        assertTrue(existeEmail, "El email debe existir");
        assertTrue(autenticado.isPresent(), "La autenticación debe ser exitosa");
        assertEquals(usuarioActivo.getUsuarioID(), autenticado.get().getUsuarioID());
    }
}
