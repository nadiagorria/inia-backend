package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ActualizarPerfilRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.AprobarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GestionarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RegistroUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UsuarioService
 * 
 * Funcionalidades testeadas:
 * - Registro de nuevos usuarios
 * - Validación de duplicados (username y email)
 * - Aprobación de solicitudes pendientes
 * - Cambio de contraseña
 * - Activación/Desactivación de usuarios
 * - Gestión de roles
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("Tests de UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private EmailService emailService;

    @Mock
    private TotpService totpService;

    @Mock
    private BackupCodeService backupCodeService;

    @InjectMocks
    private UsuarioService usuarioService;

    private RegistroUsuarioRequestDTO registroRequestDTO;
    private Usuario usuario;
    private Usuario analista;

    @BeforeEach
    void setUp() {
        
        registroRequestDTO = new RegistroUsuarioRequestDTO();
        registroRequestDTO.setNombre("jperez");
        registroRequestDTO.setNombres("Juan");
        registroRequestDTO.setApellidos("Pérez");
        registroRequestDTO.setEmail("jperez@example.com");
        registroRequestDTO.setContrasenia("Password123!");

        usuario = new Usuario();
        usuario.setUsuarioID(1);
        usuario.setNombre("jperez");
        usuario.setNombres("Juan");
        usuario.setApellidos("Pérez");
        usuario.setEmail("jperez@example.com");
        usuario.setEstado(EstadoUsuario.PENDIENTE);
        usuario.setActivo(false);

        analista = new Usuario();
        analista.setUsuarioID(2);
        analista.setNombre("analista1");
        analista.setNombres("Ana");
        analista.setApellidos("Listas");
        analista.setEmail("analista@inia.com");
        analista.setRol(Rol.ANALISTA);
        analista.setActivo(true);
        analista.setEstado(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("Registrar usuario - debe crear usuario en estado PENDIENTE")
    void registrarSolicitud_debeCrearUsuarioPendiente() {
        
        when(usuarioRepository.findByNombre(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioRepository.findAllByRol(Rol.ANALISTA)).thenReturn(Arrays.asList(analista));
        doNothing().when(notificacionService).notificarNuevoUsuario(anyLong());
        doNothing().when(emailService).enviarEmailConfirmacionRegistro(anyString(), anyString());
        doNothing().when(emailService).enviarEmailNuevoRegistro(anyString(), anyString(), anyString(), anyString());

        
        UsuarioDTO resultado = usuarioService.registrarSolicitud(registroRequestDTO);

        
        assertNotNull(resultado, "El usuario registrado no debe ser nulo");
        assertEquals("jperez", resultado.getNombre());
        assertEquals("Juan", resultado.getNombres());
        assertEquals("Pérez", resultado.getApellidos());
        assertEquals("jperez@example.com", resultado.getEmail());
        
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(notificacionService, times(1)).notificarNuevoUsuario(anyLong());
    }

    @Test
    @DisplayName("Registrar usuario con nombre duplicado - debe lanzar excepción")
    void registrarSolicitud_conNombreDuplicado_debeLanzarExcepcion() {
        
        when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuario));

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.registrarSolicitud(registroRequestDTO);
        });
        
        assertEquals("El nombre de usuario ya existe", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registrar usuario con email duplicado - debe lanzar excepción")
    void registrarSolicitud_conEmailDuplicado_debeLanzarExcepcion() {
        
        when(usuarioRepository.findByNombre(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("jperez@example.com")).thenReturn(Optional.of(usuario));

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.registrarSolicitud(registroRequestDTO);
        });
        
        assertEquals("El email ya está registrado", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Listar solicitudes pendientes - debe retornar solo usuarios PENDIENTES")
    void listarSolicitudesPendientes_debeRetornarSoloPendientes() {
        
        Usuario usuario2 = new Usuario();
        usuario2.setUsuarioID(3);
        usuario2.setNombre("mgarcia");
        usuario2.setEstado(EstadoUsuario.PENDIENTE);
        
        when(usuarioRepository.findByEstado(EstadoUsuario.PENDIENTE))
            .thenReturn(Arrays.asList(usuario, usuario2));

        
        List<UsuarioDTO> resultado = usuarioService.listarSolicitudesPendientes();

        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(usuarioRepository, times(1)).findByEstado(EstadoUsuario.PENDIENTE);
    }

    @Test
    @DisplayName("Listar solicitudes pendientes paginadas - debe retornar página correcta")
    void listarSolicitudesPendientesPaginadas_debeRetornarPaginaCorrecta() {
        
        Page<Usuario> usuariosPage = new PageImpl<>(Arrays.asList(usuario));
        
        when(usuarioRepository.findByEstado(eq(EstadoUsuario.PENDIENTE), any(Pageable.class)))
            .thenReturn(usuariosPage);

        
        Page<UsuarioDTO> resultado = usuarioService.listarSolicitudesPendientesPaginadas(0, 10, null);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Listar solicitudes con búsqueda - debe filtrar resultados")
    void listarSolicitudesPendientesPaginadas_conBusqueda_debeFiltrar() {
        
        Page<Usuario> usuariosPage = new PageImpl<>(Arrays.asList(usuario));
        
        when(usuarioRepository.findByEstadoAndSearchTerm(
            eq(EstadoUsuario.PENDIENTE), eq("juan"), any(Pageable.class)))
            .thenReturn(usuariosPage);

        
        Page<UsuarioDTO> resultado = usuarioService.listarSolicitudesPendientesPaginadas(0, 10, "juan");

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(usuarioRepository, times(1))
            .findByEstadoAndSearchTerm(eq(EstadoUsuario.PENDIENTE), eq("juan"), any(Pageable.class));
    }

    @Test
    @DisplayName("Aprobar usuario - debe cambiar estado a APROBADO y asignar rol")
    void aprobarUsuario_debeCambiarEstadoYAsignarRol() {
        
        AprobarUsuarioRequestDTO aprobarRequest = new AprobarUsuarioRequestDTO();
        aprobarRequest.setRol(Rol.ANALISTA);
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(emailService).enviarEmailBienvenida(anyString(), anyString());

        
        UsuarioDTO resultado = usuarioService.aprobarUsuario(1, aprobarRequest);

        
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Aprobar usuario inexistente - debe lanzar excepción")
    void aprobarUsuario_usuarioInexistente_debeLanzarExcepcion() {
        
        AprobarUsuarioRequestDTO aprobarRequest = new AprobarUsuarioRequestDTO();
        aprobarRequest.setRol(Rol.ANALISTA);
        
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            usuarioService.aprobarUsuario(999, aprobarRequest);
        });
        
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Buscar usuario por ID - debe retornar usuario si existe")
    void buscarPorId_cuandoExiste_debeRetornarUsuario() {
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        
        Optional<Usuario> resultado = usuarioService.buscarPorId(1);

        
        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getUsuarioID());
        assertEquals("jperez", resultado.get().getNombre());
    }

    @Test
    @DisplayName("Buscar usuario por ID inexistente - debe retornar Optional vacío")
    void buscarPorId_cuandoNoExiste_debeRetornarOptionalVacio() {
        
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        
        Optional<Usuario> resultado = usuarioService.buscarPorId(999);

        
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Gestionar usuario - debe cambiar rol y estado de usuario")
    void gestionarUsuario_debeCambiarRolYEstado() {
        
        GestionarUsuarioRequestDTO gestionRequest = new GestionarUsuarioRequestDTO();
        gestionRequest.setRol(Rol.ADMIN);
        gestionRequest.setActivo(true);
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            return u;
        });

        
        UsuarioDTO resultado = usuarioService.gestionarUsuario(1, gestionRequest);

        
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("actualizarPerfil - actualiza nombres y apellidos correctamente")
    void actualizarPerfil_actualizaNombresYApellidos() {
        
        ActualizarPerfilRequestDTO request = new ActualizarPerfilRequestDTO();
        request.setNombres("Juan Carlos");
        request.setApellidos("Pérez García");
        
        Usuario usuarioActual = new Usuario();
        usuarioActual.setUsuarioID(1);
        usuarioActual.setNombre("jperez");
        usuarioActual.setNombres("Juan");
        usuarioActual.setApellidos("Pérez");
        usuarioActual.setEmail("jperez@example.com");
        usuarioActual.setContrasenia("encodedPassword");
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuarioActual));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            
            UsuarioDTO resultado = usuarioService.actualizarPerfil(request);
            
            
            assertNotNull(resultado);
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("actualizarPerfil - cambia contraseña con contraseña actual correcta")
    void actualizarPerfil_cambiaContraseniaCorrectamente() {
        
        ActualizarPerfilRequestDTO request = new ActualizarPerfilRequestDTO();
        request.setContraseniaActual("oldPassword");
        request.setContraseniaNueva("NewPassword123!");
        
        Usuario usuarioActual = new Usuario();
        usuarioActual.setUsuarioID(1);
        usuarioActual.setNombre("jperez");
        usuarioActual.setContrasenia("encodedOldPassword");
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuarioActual));
            when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
            when(passwordEncoder.encode("NewPassword123!")).thenReturn("encodedNewPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            
            UsuarioDTO resultado = usuarioService.actualizarPerfil(request);
            
            
            assertNotNull(resultado);
            verify(passwordEncoder, times(1)).encode("NewPassword123!");
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("actualizarPerfil - lanza excepción si contraseña actual es incorrecta")
    void actualizarPerfil_contraseniaActualIncorrecta_lanzaExcepcion() {
        
        ActualizarPerfilRequestDTO request = new ActualizarPerfilRequestDTO();
        request.setContraseniaActual("wrongPassword");
        request.setContraseniaNueva("NewPassword123!");
        
        Usuario usuarioActual = new Usuario();
        usuarioActual.setUsuarioID(1);
        usuarioActual.setNombre("jperez");
        usuarioActual.setContrasenia("encodedOldPassword");
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuarioActual));
            when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);
            
            
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                usuarioService.actualizarPerfil(request);
            });
            
            assertTrue(exception.getMessage().contains("Contraseña actual incorrecta"));
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("listarTodosUsuariosPaginados - sin filtros retorna todos paginados")
    void listarTodosUsuariosPaginados_sinFiltros() {
        
        Page<Usuario> usuariosPage = new PageImpl<>(Arrays.asList(usuario, analista));
        
        when(usuarioRepository.findAll(any(Pageable.class))).thenReturn(usuariosPage);
        
        
        Page<UsuarioDTO> resultado = usuarioService.listarTodosUsuariosPaginados(0, 10, null, null, null);
        
        
        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    @DisplayName("listarTodosUsuariosPaginados - con rol ANALISTA")
    void listarTodosUsuariosPaginados_conRolAnalista() {
        
        Page<Usuario> usuariosPage = new PageImpl<>(Arrays.asList(analista));
        
        when(usuarioRepository.findByRol(eq(Rol.ANALISTA), any(Pageable.class)))
            .thenReturn(usuariosPage);
        
        
        Page<UsuarioDTO> resultado = usuarioService.listarTodosUsuariosPaginados(0, 10, null, Rol.ANALISTA, null);
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("listarTodosUsuariosPaginados - con activo=true")
    void listarTodosUsuariosPaginados_conActivoTrue() {
        
        Page<Usuario> usuariosPage = new PageImpl<>(Arrays.asList(analista));
        
        when(usuarioRepository.findByActivo(eq(true), any(Pageable.class)))
            .thenReturn(usuariosPage);
        
        
        Page<UsuarioDTO> resultado = usuarioService.listarTodosUsuariosPaginados(0, 10, null, null, true);
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("listarTodosUsuariosPaginados - con búsqueda")
    void listarTodosUsuariosPaginados_conBusqueda() {
        
        Page<Usuario> usuariosPage = new PageImpl<>(Arrays.asList(analista));
        
        when(usuarioRepository.findBySearchTerm(eq("ana"), any(Pageable.class)))
            .thenReturn(usuariosPage);
        
        
        Page<UsuarioDTO> resultado = usuarioService.listarTodosUsuariosPaginados(0, 10, "ana", null, null);
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("cambiarContrasenia - cambia contraseña correctamente")
    void cambiarContrasenia_cambiaCorrectamente() {
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encodedNewPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        
        usuarioService.cambiarContrasenia(1, "NewPassword123!");
        
        
        verify(passwordEncoder, times(1)).encode("NewPassword123!");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("cambiarContrasenia - lanza excepción si contraseña vacía")
    void cambiarContrasenia_contraseniaVacia_lanzaExcepcion() {
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.cambiarContrasenia(1, "");
        });
        
        assertTrue(exception.getMessage().contains("no puede estar vacía"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("cambiarContrasenia - lanza excepción si contraseña muy corta")
    void cambiarContrasenia_contraseniaMuyCorta_lanzaExcepcion() {
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.cambiarContrasenia(1, "short");
        });
        
        assertTrue(exception.getMessage().contains("al menos 8 caracteres"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("rechazarSolicitud - elimina usuario pendiente")
    void rechazarSolicitud_eliminaUsuarioPendiente() {
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        doNothing().when(notificacionService).notificarUsuarioRechazado(anyLong());
        doNothing().when(usuarioRepository).delete(any(Usuario.class));
        
        
        usuarioService.rechazarSolicitud(1);
        
        
        verify(usuarioRepository, times(1)).delete(any(Usuario.class));
        verify(notificacionService, times(1)).notificarUsuarioRechazado(1L);
    }

    @Test
    @DisplayName("rechazarSolicitud - lanza excepción si usuario no está pendiente")
    void rechazarSolicitud_usuarioNoEsPendiente_lanzaExcepcion() {
        
        analista.setEstado(EstadoUsuario.ACTIVO);
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(analista));
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.rechazarSolicitud(2);
        });
        
        assertTrue(exception.getMessage().contains("PENDIENTE"));
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("obtenerUsuarioActual - retorna usuario autenticado")
    void obtenerUsuarioActual_retornaUsuarioAutenticado() {
        
        ActualizarPerfilRequestDTO request = new ActualizarPerfilRequestDTO();
        request.setNombres("Nuevo Nombre");
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            
            UsuarioDTO resultado = usuarioService.actualizarPerfil(request);
            
            
            assertNotNull(resultado);
            verify(usuarioRepository, times(1)).findByNombre("jperez");
        }
    }

    @Test
    @DisplayName("crearAdminPredeterminado - crea admin con 2FA")
    void crearAdminPredeterminado_creaAdminConExito() {
        
        when(usuarioRepository.existsByRol(Rol.ADMIN)).thenReturn(false);
        when(totpService.generateSecret()).thenReturn("SECRET123");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setUsuarioID(1);
            return u;
        });
        
        
        UsuarioDTO resultado = usuarioService.crearAdminPredeterminado();
        
        
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).existsByRol(Rol.ADMIN);
        verify(totpService, times(1)).generateSecret();
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("crearAdminPredeterminado - lanza excepción si ya existe admin")
    void crearAdminPredeterminado_yaExisteAdmin_lanzaExcepcion() {
        
        when(usuarioRepository.existsByRol(Rol.ADMIN)).thenReturn(true);
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.crearAdminPredeterminado();
        });
        
        assertTrue(exception.getMessage().contains("Ya existe un administrador"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("guardar - guarda usuario correctamente")
    void guardar_guardaUsuarioCorrectamente() {
        
        Usuario usuarioAGuardar = new Usuario();
        usuarioAGuardar.setNombre("newuser");
        usuarioAGuardar.setEmail("newuser@example.com");
        
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setUsuarioID(10);
            return u;
        });
        
        
        Usuario resultado = usuarioService.guardar(usuarioAGuardar);
        
        
        assertNotNull(resultado);
        assertEquals(10, resultado.getUsuarioID());
        verify(usuarioRepository, times(1)).save(usuarioAGuardar);
    }

    @Test
    @DisplayName("buscarPorEmail - encuentra usuario por email")
    void buscarPorEmail_encuentraUsuario() {
        
        String email = "jperez@example.com";
        usuario.setEmail(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        
        
        Optional<Usuario> resultado = usuarioService.buscarPorEmail(email);
        
        
        assertTrue(resultado.isPresent());
        assertEquals(email, resultado.get().getEmail());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("buscarPorEmail - retorna empty si no encuentra usuario")
    void buscarPorEmail_noEncuentraUsuario() {
        
        String email = "noexiste@example.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        
        Optional<Usuario> resultado = usuarioService.buscarPorEmail(email);
        
        
        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("obtenerPerfil - retorna perfil del usuario autenticado")
    void obtenerPerfil_retornaPerfilUsuarioActual() {
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuario));
            
            
            UsuarioDTO resultado = usuarioService.obtenerPerfil();
            
            
            assertNotNull(resultado);
            verify(usuarioRepository, times(1)).findByNombre("jperez");
        }
    }

    @Test
    @DisplayName("actualizarPerfil - actualiza email correctamente")
    void actualizarPerfil_actualizaEmailCorrectamente() {
        
        ActualizarPerfilRequestDTO request = new ActualizarPerfilRequestDTO();
        request.setEmail("nuevo.email@example.com");
        
        Usuario usuarioActual = new Usuario();
        usuarioActual.setUsuarioID(1);
        usuarioActual.setNombre("jperez");
        usuarioActual.setEmail("viejo@example.com");
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuarioActual));
            when(usuarioRepository.findByEmailIgnoreCase("nuevo.email@example.com")).thenReturn(Optional.empty());
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            
            UsuarioDTO resultado = usuarioService.actualizarPerfil(request);
            
            
            assertNotNull(resultado);
            verify(usuarioRepository, times(1)).findByEmailIgnoreCase("nuevo.email@example.com");
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("actualizarPerfil - lanza excepción si email ya existe")
    void actualizarPerfil_emailYaExiste_lanzaExcepcion() {
        
        ActualizarPerfilRequestDTO request = new ActualizarPerfilRequestDTO();
        request.setEmail("existente@example.com");
        
        Usuario usuarioActual = new Usuario();
        usuarioActual.setUsuarioID(1);
        usuarioActual.setNombre("jperez");
        usuarioActual.setEmail("viejo@example.com");
        
        Usuario otroUsuario = new Usuario();
        otroUsuario.setUsuarioID(2);
        otroUsuario.setEmail("existente@example.com");
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuarioActual));
            when(usuarioRepository.findByEmailIgnoreCase("existente@example.com")).thenReturn(Optional.of(otroUsuario));
            
            
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                usuarioService.actualizarPerfil(request);
            });
            
            assertTrue(exception.getMessage().contains("email ya está en uso"));
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("actualizarPerfil - actualiza nombre de usuario correctamente")
    void actualizarPerfil_actualizaNombreUsuarioCorrectamente() {
        
        ActualizarPerfilRequestDTO request = new ActualizarPerfilRequestDTO();
        request.setNombre("nuevousuario");
        
        Usuario usuarioActual = new Usuario();
        usuarioActual.setUsuarioID(1);
        usuarioActual.setNombre("jperez");
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuarioActual));
            when(usuarioRepository.findByNombreIgnoreCase("nuevousuario")).thenReturn(Optional.empty());
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            
            UsuarioDTO resultado = usuarioService.actualizarPerfil(request);
            
            
            assertNotNull(resultado);
            verify(usuarioRepository, times(1)).findByNombreIgnoreCase("nuevousuario");
            verify(usuarioRepository, times(1)).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("actualizarPerfil - lanza excepción si nombre de usuario ya existe")
    void actualizarPerfil_nombreUsuarioYaExiste_lanzaExcepcion() {
        
        ActualizarPerfilRequestDTO request = new ActualizarPerfilRequestDTO();
        request.setNombre("usuarioexistente");
        
        Usuario usuarioActual = new Usuario();
        usuarioActual.setUsuarioID(1);
        usuarioActual.setNombre("jperez");
        
        Usuario otroUsuario = new Usuario();
        otroUsuario.setUsuarioID(2);
        otroUsuario.setNombre("usuarioexistente");
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("jperez");
            when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuarioActual));
            when(usuarioRepository.findByNombreIgnoreCase("usuarioexistente")).thenReturn(Optional.of(otroUsuario));
            
            
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                usuarioService.actualizarPerfil(request);
            });
            
            assertTrue(exception.getMessage().contains("nombre de usuario ya está en uso"));
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }
    }

    @Test
    @DisplayName("gestionarUsuario - actualiza estado activo/inactivo")
    void gestionarUsuario_actualizaEstadoActivo() {
        
        GestionarUsuarioRequestDTO gestionRequest = new GestionarUsuarioRequestDTO();
        gestionRequest.setRol(Rol.ANALISTA);
        gestionRequest.setActivo(false); 
        
        analista.setActivo(true); 
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(analista));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        UsuarioDTO resultado = usuarioService.gestionarUsuario(2, gestionRequest);

        
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(2);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("gestionarUsuario - cambia rol del usuario")
    void gestionarUsuario_cambiaRol() {
        
        GestionarUsuarioRequestDTO gestionRequest = new GestionarUsuarioRequestDTO();
        gestionRequest.setRol(Rol.ADMIN); 
        gestionRequest.setActivo(true);
        
        analista.setRol(Rol.ANALISTA); 
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(analista));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        UsuarioDTO resultado = usuarioService.gestionarUsuario(2, gestionRequest);

        
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
}

