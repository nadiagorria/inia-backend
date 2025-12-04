package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ActualizarPerfilRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.AprobarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ForgotPasswordRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GestionarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoginRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RegistroUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ResetPasswordRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.Verify2FARequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.Setup2FAResponseDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TrustedDeviceDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;
import utec.proyectofinal.Proyecto.Final.UTEC.security.SeguridadService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.EmailService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RecoveryCodeService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TotpService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TrustedDeviceService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.UsuarioService;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000") 
@Tag(name = "Autenticación y Gestión de Usuarios", description = "Endpoints para autenticación, registro y gestión de usuarios")
public class AuthController {

    @Autowired
    private SeguridadService seguridadService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TotpService totpService;

    @Autowired
    private RecoveryCodeService recoveryCodeService;

    @Autowired
    private TrustedDeviceService trustedDeviceService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT en cookies HttpOnly")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginData, HttpServletResponse response) {
        System.out.println(" [LOGIN] Iniciando proceso de login...");
        System.out.println(" [LOGIN] Usuario recibido: " + loginData.getUsuario());
        
        try {
            String usuario = loginData.getUsuario();
            String password = loginData.getPassword();
            
            Optional<Usuario> usuarioOpt = seguridadService.autenticarUsuario(usuario, password);
            
            System.out.println(" [LOGIN] Autenticación completada. Usuario encontrado: " + usuarioOpt.isPresent());

            if (usuarioOpt.isPresent()) {
                Usuario user = usuarioOpt.get();
                String[] roles = seguridadService.listarRolesPorUsuario(user);
            
                
                String accessToken = jwtUtil.generarToken(user, java.util.Arrays.asList(roles));
                String refreshToken = jwtUtil.generarRefreshToken(user);
                
                
                configurarCookieToken(response, "accessToken", accessToken, (int) (jwtUtil.getAccessTokenExpiration() / 1000));
                configurarCookieToken(response, "refreshToken", refreshToken, (int) (jwtUtil.getRefreshTokenExpiration() / 1000));
                
                
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("mensaje", "Login exitoso");
                responseBody.put("usuario", Map.of(
                    "id", user.getUsuarioID(),
                    "nombre", user.getNombre(),
                    "nombres", user.getNombres(),
                    "apellidos", user.getApellidos(),
                    "email", user.getEmail(),
                    "roles", roles
                ));
                
                return ResponseEntity.ok(responseBody);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
                    
        } catch (RuntimeException e) {
            System.err.println(" [LOGIN] Error en autenticación: " + e.getMessage());
            e.printStackTrace();
            
            String mensaje = switch (e.getMessage()) {
                case "USUARIO_INCORRECTO" -> "Credenciales incorrectas";
                case "USUARIO_INACTIVO" -> "No se puede iniciar sesión. Contacte al administrador";
                case "USUARIO_PENDIENTE_APROBACION" -> "Cuenta pendiente de aprobación. Contacte al administrador";
                case "USUARIO_SIN_ROL" -> "No se puede iniciar sesión. Contacte al administrador";
                case "CONTRASENIA_INCORRECTA" -> "Credenciales incorrectas";
                default -> "Error de autenticación";
            };
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", mensaje));
        }
    }

    
    private void configurarCookieToken(HttpServletResponse response, String nombre, String valor, int maxAgeSegundos) {
        
        String cookieValue = String.format(
            "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
            nombre, valor, maxAgeSegundos
        );
        
        System.out.println(" [AuthController] Estableciendo cookie: " + nombre + " (maxAge: " + maxAgeSegundos + "s)");
        
        response.addHeader("Set-Cookie", cookieValue);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token", description = "Usa el refresh token para generar un nuevo access token")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token no encontrado"));
            }

            if (!jwtUtil.esTokenValido(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token inválido o expirado"));
            }

            String tipo = jwtUtil.obtenerTipoToken(refreshToken);
            if (!"refresh".equals(tipo)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token no es de tipo refresh"));
            }

            Integer userId = jwtUtil.obtenerUserIdDelToken(refreshToken);
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario user = usuarioOpt.get();
            String[] roles = seguridadService.listarRolesPorUsuario(user);

            
            String nuevoAccessToken = jwtUtil.generarToken(user, java.util.Arrays.asList(roles));
            configurarCookieToken(response, "accessToken", nuevoAccessToken, (int) (jwtUtil.getAccessTokenExpiration() / 1000));

            return ResponseEntity.ok(Map.of("mensaje", "Access token renovado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Error al renovar token: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida las cookies de autenticación")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        
        Cookie accessCookie = new Cookie("accessToken", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(Map.of("mensaje", "Logout exitoso"));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida si un token JWT es válido")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            
            if (jwtUtil.esTokenValido(token)) {
                String username = jwtUtil.obtenerUsuarioDelToken(token);
                return ResponseEntity.ok(Map.of(
                    "valido", true,
                    "usuario", username
                ));
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "error", "Token inválido"));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "error", "Token malformado"));
        }
    }

    

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Solicita el registro de un nuevo usuario (pendiente de aprobación)")
    public ResponseEntity<?> registrar(@RequestBody RegistroUsuarioRequestDTO solicitud) {
        try {
            UsuarioDTO usuarioRegistrado = usuarioService.registrarSolicitud(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Solicitud de registro enviada. Pendiente de aprobación por el administrador.",
                        "usuario", usuarioRegistrado
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar solicitudes pendientes", description = "Lista todas las solicitudes de registro pendientes de aprobación")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UsuarioDTO>> listarSolicitudesPendientes() {
        List<UsuarioDTO> solicitudes = usuarioService.listarSolicitudesPendientes();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/pending/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar solicitudes pendientes paginadas", description = "Lista solicitudes pendientes con paginación y búsqueda")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<UsuarioDTO>> listarSolicitudesPendientesPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<UsuarioDTO> solicitudes = usuarioService.listarSolicitudesPendientesPaginadas(page, size, search);
        return ResponseEntity.ok(solicitudes);
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar usuario", description = "Aprueba un usuario registrado y le asigna un rol")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> aprobarUsuario(@PathVariable Integer id, @RequestBody AprobarUsuarioRequestDTO solicitud) {
        try {
            UsuarioDTO usuarioAprobado = usuarioService.aprobarUsuario(id, solicitud);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario aprobado exitosamente",
                "usuario", usuarioAprobado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar solicitud", description = "Rechaza y elimina una solicitud de registro")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> rechazarSolicitud(@PathVariable Integer id) {
        try {
            usuarioService.rechazarSolicitud(id);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud rechazada"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios del sistema")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.listarTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/users/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios paginados", description = "Lista todos los usuarios con paginación, búsqueda y filtros por rol y estado activo")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<UsuarioDTO>> listarUsuariosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String activo) {
        
        
        utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol rolEnum = null;
        if (rol != null && !rol.trim().isEmpty() && !rol.equalsIgnoreCase("all")) {
            try {
                rolEnum = utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.valueOf(rol.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
        
        
        Boolean activoBoolean = null;
        if (activo != null && !activo.trim().isEmpty() && !activo.equalsIgnoreCase("all")) {
            activoBoolean = Boolean.parseBoolean(activo);
        }
        
        Page<UsuarioDTO> usuarios = usuarioService.listarTodosUsuariosPaginados(page, size, search, rolEnum, activoBoolean);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/users/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios activos", description = "Lista todos los usuarios activos del sistema")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UsuarioDTO>> listarUsuariosActivos() {
        List<UsuarioDTO> usuarios = usuarioService.listarUsuariosActivos();
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gestionar usuario", description = "Actualiza el rol o estado de un usuario")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> gestionarUsuario(@PathVariable Integer id, @RequestBody GestionarUsuarioRequestDTO solicitud) {
        try {
            UsuarioDTO usuarioActualizado = usuarioService.gestionarUsuario(id, solicitud);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario actualizado exitosamente",
                "usuario", usuarioActualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener perfil", description = "Obtiene el perfil del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> obtenerPerfil() {
        try {
            UsuarioDTO perfil = usuarioService.obtenerPerfil();
            return ResponseEntity.ok(perfil);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Actualizar perfil", description = "Actualiza el perfil del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> actualizarPerfil(@RequestBody ActualizarPerfilRequestDTO solicitud, HttpServletResponse response) {
        try {
            UsuarioDTO perfilActualizado = usuarioService.actualizarPerfil(solicitud);
            
            
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(perfilActualizado.getUsuarioID());
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String[] roles = seguridadService.listarRolesPorUsuario(usuario);
                
                
                String nuevoAccessToken = jwtUtil.generarToken(usuario, java.util.Arrays.asList(roles));
                String nuevoRefreshToken = jwtUtil.generarRefreshToken(usuario);
                
                
                configurarCookieToken(response, "accessToken", nuevoAccessToken, (int) (jwtUtil.getAccessTokenExpiration() / 1000));
                configurarCookieToken(response, "refreshToken", nuevoRefreshToken, (int) (jwtUtil.getRefreshTokenExpiration() / 1000));
                
                System.out.println(" [PERFIL] Tokens actualizados después de modificar perfil para usuario: " + usuario.getNombre());
            }
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Perfil actualizado exitosamente",
                "usuario", perfilActualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    

    @PostMapping("/init-admin")
    @Operation(summary = "Crear admin inicial", description = "Crea el administrador predeterminado si no existe")
    public ResponseEntity<?> crearAdminPredeterminado() {
        try {
            UsuarioDTO admin = usuarioService.crearAdminPredeterminado();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Administrador predeterminado creado. Usuario: 'admin', Contraseña: 'admin123'",
                        "usuario", admin
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    

    @GetMapping("/check-username")
    @Operation(summary = "Verificar disponibilidad de nombre de usuario", description = "Verifica si un nombre de usuario está disponible")
    public ResponseEntity<?> verificarNombreUsuario(@RequestParam String nombre) {
        try {
            boolean disponible = usuarioService.esNombreUsuarioDisponible(nombre);
            return ResponseEntity.ok(Map.of("disponible", disponible));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al verificar nombre de usuario"));
        }
    }

    @GetMapping("/check-email")
    @Operation(summary = "Verificar disponibilidad de email", description = "Verifica si un email está disponible")
    public ResponseEntity<?> verificarEmail(@RequestParam String email) {
        try {
            boolean disponible = usuarioService.esEmailDisponible(email);
            return ResponseEntity.ok(Map.of("disponible", disponible));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al verificar email"));
        }
    }
}