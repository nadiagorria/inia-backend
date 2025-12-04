package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ForgotPasswordRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.Login2FARequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ResetPasswordRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.Verify2FARequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.Setup2FAResponseDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TrustedDeviceDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.exceptions.BadRequestException;
import utec.proyectofinal.Proyecto.Final.UTEC.exceptions.NotFoundException;
import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;
import utec.proyectofinal.Proyecto.Final.UTEC.security.SeguridadService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.EmailService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RecoveryCodeService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TotpService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TrustedDeviceService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.UsuarioService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.BackupCodeService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.SetupTokenService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Autenticación 2FA y Recuperación", description = "Endpoints para 2FA y recuperación de contraseña")
public class Auth2FAController {

    @Autowired
    private SeguridadService seguridadService;

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

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BackupCodeService backupCodeService;

    @Autowired
    private SetupTokenService setupTokenService;

    

    @PostMapping("/login-2fa")
    @Operation(summary = "Login con soporte 2FA", description = "Autentica usuario con soporte para 2FA y dispositivos de confianza")
    public ResponseEntity<?> loginWith2FA(@RequestBody Login2FARequestDTO loginData, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) {
        // PASO 1: Autenticar credenciales básicas (usuario + contraseña)
        Optional<Usuario> usuarioOpt = seguridadService.autenticarUsuario(
            loginData.getUsuario(), 
            loginData.getPassword()
        );
        
        if (usuarioOpt.isEmpty()) {
            throw new BadRequestException("Credenciales incorrectas");
        }
        
        Usuario user = usuarioOpt.get();
        
        // PASO 2: Verificar si el usuario requiere cambio de credenciales (primer login del admin)
        if (user.getRequiereCambioCredenciales() != null && user.getRequiereCambioCredenciales()) {
            String qrCodeDataUrl = totpService.generateQrCodeDataUrl(user.getTotpSecret(), "admin@temporal.local");
            
            String setupToken = setupTokenService.createSetupToken(
                user.getUsuarioID(),
                user.getNombre(),
                qrCodeDataUrl,
                user.getTotpSecret()
            );
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "requiresCredentialChange", true,
                        "mensaje", "Debes configurar tus credenciales y 2FA en el primer acceso",
                        "setupToken", setupToken
                    ));
        }
        
        // PASO 3: Verificar si el usuario tiene 2FA habilitado
        boolean has2FAEnabled = user.getTotpEnabled() != null && user.getTotpEnabled();
        
        if (!has2FAEnabled) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "requires2FASetup", true,
                        "mensaje", "Debes activar la autenticación de dos factores para usar el sistema",
                        "userId", user.getUsuarioID(),
                        "email", user.getEmail(),
                        "nombre", user.getNombreCompleto()
                    ));
        }
        
        // PASO 4: Verificar dispositivo de confianza
        String deviceFingerprint = loginData.getDeviceFingerprint();
        boolean isTrustedDevice = false;
        
        if (deviceFingerprint != null && !deviceFingerprint.isEmpty()) {
            isTrustedDevice = trustedDeviceService.isTrustedDevice(user.getUsuarioID(), deviceFingerprint);
        }
        
        if (isTrustedDevice) {
            return completarLoginExitoso(user, response);
        }
        
        // PASO 5: Dispositivo NO es de confianza - Requiere código 2FA
        String totpCode = loginData.getTotpCode();
        
        if (totpCode == null || totpCode.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "requires2FA", true,
                        "mensaje", "Se requiere código de autenticación de dos factores",
                        "userId", user.getUsuarioID()
                    ));
        }
        
        // PASO 6: Verificar código 2FA (TOTP o código de respaldo)
        boolean isCodeValid = false;
        
        if (totpCode.length() == 6 && totpCode.matches("\\d+")) {
            isCodeValid = totpService.verifyCode(user.getTotpSecret(), totpCode);
        }
        
        if (!isCodeValid && totpCode.length() >= 12) {
            isCodeValid = backupCodeService.verifyAndUseBackupCode(user.getUsuarioID(), totpCode);
        }
        
        if (!isCodeValid) {
            throw new BadRequestException("Código de autenticación inválido");
        }
        
        // PASO 7: Si el usuario quiere confiar en este dispositivo, registrarlo
        if (loginData.getTrustDevice() != null && loginData.getTrustDevice() && deviceFingerprint != null) {
            trustedDeviceService.trustDevice(user.getUsuarioID(), deviceFingerprint, request);
            
            String deviceName = extractDeviceName(request);
            String ipAddress = extractIpAddress(request);
            emailService.enviarNuevoDispositivo(
                user.getEmail(),
                user.getNombreCompleto(),
                deviceName,
                ipAddress
            );
        }
        
        return completarLoginExitoso(user, response);
    }

    
    private ResponseEntity<?> completarLoginExitoso(Usuario user, HttpServletResponse response) {
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
            "roles", roles,
            "has2FA", user.getTotpEnabled() != null && user.getTotpEnabled()
        ));
        
        return ResponseEntity.ok(responseBody);
    }

    
    private void configurarCookieToken(HttpServletResponse response, String nombre, String valor, int maxAgeSegundos) {
        String cookieValue = String.format(
            "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
            nombre, valor, maxAgeSegundos
        );
        response.addHeader("Set-Cookie", cookieValue);
    }

    
    private String extractDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Dispositivo Desconocido";
        
        String browser = "Navegador";
        String os = "SO";
        
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) browser = "Chrome";
        else if (userAgent.contains("Firefox")) browser = "Firefox";
        else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) browser = "Safari";
        else if (userAgent.contains("Edg")) browser = "Edge";
        
        if (userAgent.contains("Windows")) os = "Windows";
        else if (userAgent.contains("Mac")) os = "macOS";
        else if (userAgent.contains("Linux")) os = "Linux";
        else if (userAgent.contains("Android")) os = "Android";
        else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS";
        
        return browser + " en " + os;
    }

    
    private String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    // ENDPOINTS DE AUTENTICACIÓN DE DOS FACTORES (2FA)

    @PostMapping("/2fa/setup-initial")
    @Operation(summary = "Setup inicial de 2FA (sin autenticación)", 
               description = "Genera QR code para usuarios que DEBEN activar 2FA por primera vez")
    public ResponseEntity<?> setupInitial2FA(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        if (email == null || password == null) {
            throw new BadRequestException("Email y contraseña son requeridos");
        }
        
        Optional<Usuario> userOpt = seguridadService.autenticarUsuario(email, password);
        
        if (userOpt.isEmpty()) {
            throw new BadRequestException("Credenciales incorrectas");
        }
        
        Usuario user = userOpt.get();
        
        if (user.getTotpEnabled() != null && user.getTotpEnabled()) {
            throw new BadRequestException("El usuario ya tiene 2FA habilitado. Inicia sesión normalmente.");
        }
        
        String secret = totpService.generateSecret();
        String accountName = user.getEmail();
        String qrCodeDataUrl = totpService.generateQrCodeDataUrl(secret, accountName);
        
        user.setTotpSecret(secret);
        user.setTotpEnabled(false);
        usuarioService.guardar(user);
        
        Setup2FAResponseDTO response = new Setup2FAResponseDTO(
            secret,
            qrCodeDataUrl,
            "INIA",
            accountName
        );
        
        return ResponseEntity.ok(Map.of(
            "mensaje", "Escanea el QR code con Google Authenticator",
            "data", response,
            "userId", user.getUsuarioID(),
            "email", user.getEmail()
        ));
    }

    @PostMapping("/2fa/verify-initial")
    @Operation(summary = "Verificar código 2FA inicial (sin autenticación)", 
               description = "Verifica el código TOTP y activa 2FA por primera vez, luego hace login")
    public ResponseEntity<?> verifyInitial2FA(
            @RequestBody Map<String, String> request,
            HttpServletResponse response) {
        String email = request.get("email");
        String totpCode = request.get("totpCode");
        
        if (email == null || totpCode == null) {
            throw new BadRequestException("Email y código TOTP son requeridos");
        }
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);
        
        if (usuarioOpt.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();

        if (usuario.getTotpSecret() == null || usuario.getTotpSecret().isEmpty()) {
            throw new BadRequestException("No se ha configurado 2FA para este usuario");
        }
        
        boolean isValid = totpService.verifyCode(usuario.getTotpSecret(), totpCode);
        
        if (!isValid) {
            throw new BadRequestException("Código de autenticación inválido");
        }
        
        usuario.setTotpEnabled(true);
        usuarioService.guardar(usuario);
        
        List<String> backupCodes = backupCodeService.generateBackupCodes(usuario.getUsuarioID());
        emailService.enviar2FAActivado(usuario.getEmail(), usuario.getNombreCompleto());

        String[] roles = seguridadService.listarRolesPorUsuario(usuario);
        String accessToken = jwtUtil.generarToken(usuario, java.util.Arrays.asList(roles));
        String refreshToken = jwtUtil.generarRefreshToken(usuario);
        
        configurarCookieToken(response, "accessToken", accessToken, 24 * 60 * 60);
        configurarCookieToken(response, "refreshToken", refreshToken, 7 * 24 * 60 * 60);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("mensaje", "2FA activado exitosamente. GUARDA estos códigos de respaldo.");
        responseData.put("totpEnabled", true);
        responseData.put("backupCodes", backupCodes);
        responseData.put("totalCodes", backupCodes.size());
        responseData.put("usuario", Map.of(
            "id", usuario.getUsuarioID(),
            "nombre", usuario.getNombre(),
            "nombres", usuario.getNombres(),
            "apellidos", usuario.getApellidos(),
            "email", usuario.getEmail(),
            "roles", roles,
            "has2FA", true
        ));

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/admin/setup-data/{token}")
    @Operation(summary = "Obtener datos de configuración con token temporal", 
               description = "Recupera datos de configuración usando un token de un solo uso")
    public ResponseEntity<?> getSetupData(@PathVariable String token) {
        Map<String, Object> setupData = setupTokenService.consumeSetupToken(token);
        
        if (setupData == null) {
            throw new BadRequestException("Token inválido o expirado");
        }
        
        return ResponseEntity.ok(setupData);
    }

    @PostMapping("/admin/complete-setup")
    @Operation(summary = "Completar configuración inicial del admin", 
               description = "Permite al admin configurar email, contraseña y 2FA en el primer acceso")
    public ResponseEntity<?> completeAdminSetup(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String currentPassword = request.get("currentPassword");
        String newEmail = request.get("newEmail");
        String newPassword = request.get("newPassword");
        String totpCode = request.get("totpCode");
        
        if (currentPassword == null || newEmail == null || newPassword == null || totpCode == null) {
            throw new BadRequestException("Todos los campos son requeridos");
        }

        Optional<Usuario> userOpt = seguridadService.autenticarUsuario("admin", currentPassword);
        
        if (userOpt.isEmpty()) {
            throw new BadRequestException("Credenciales incorrectas");
        }
        
        Usuario admin = userOpt.get();
        
        if (!admin.esAdmin() || !admin.getRequiereCambioCredenciales()) {
            throw new BadRequestException("No autorizado para esta acción");
        }

        boolean isValid = totpService.verifyCode(admin.getTotpSecret(), totpCode);

        if (!isValid) {
            throw new BadRequestException("Código de autenticación inválido");
        }

        admin.setEmail(newEmail);
        admin.setTotpEnabled(true);
        admin.setRequiereCambioCredenciales(false);
        usuarioService.guardar(admin);
        
        usuarioService.cambiarContrasenia(admin.getUsuarioID(), newPassword);

        List<String> backupCodes = backupCodeService.generateBackupCodes(admin.getUsuarioID());

        try {
            emailService.enviar2FAActivado(newEmail, admin.getNombreCompleto());
        } catch (Exception e) {
            // Email no crítico, continuar
        }

        String[] roles = seguridadService.listarRolesPorUsuario(admin);
        
        Collection<org.springframework.security.core.GrantedAuthority> authorities = 
            java.util.Arrays.stream(roles)
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(java.util.stream.Collectors.toList());
        
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(admin.getNombre(), null, authorities);
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);
        
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

        String accessToken = jwtUtil.generarToken(admin, java.util.Arrays.asList(roles));
        String refreshToken = jwtUtil.generarRefreshToken(admin);
        
        configurarCookieToken(response, "accessToken", accessToken, 24 * 60 * 60);
        configurarCookieToken(response, "refreshToken", refreshToken, 7 * 24 * 60 * 60);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("mensaje", "Configuración completada exitosamente");
        responseData.put("backupCodes", backupCodes);
        responseData.put("totalCodes", backupCodes.size());
        responseData.put("usuario", Map.of(
            "id", admin.getUsuarioID(),
            "nombre", admin.getNombre(),
            "nombres", admin.getNombres(),
            "apellidos", admin.getApellidos(),
            "email", admin.getEmail(),
            "roles", roles,
            "has2FA", true
        ));

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/2fa/setup")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Configurar 2FA", description = "Genera QR code para configurar Google Authenticator")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> setup2FA() {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
        
        if (usuarioOpt.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (usuario.getTotpEnabled() != null && usuario.getTotpEnabled()) {
            throw new BadRequestException("2FA ya está habilitado. Deshabilítalo primero si deseas reconfigurarlo.");
        }
        
        String secret = totpService.generateSecret();
        String accountName = usuario.getEmail();
        String qrCodeDataUrl = totpService.generateQrCodeDataUrl(secret, accountName);
        
        usuario.setTotpSecret(secret);
        usuario.setTotpEnabled(false);
        usuarioService.guardar(usuario);
        
        Setup2FAResponseDTO response = new Setup2FAResponseDTO(
            secret,
            qrCodeDataUrl,
            "INIA",
            accountName
        );

        return ResponseEntity.ok(Map.of(
            "mensaje", "Escanea el QR code con Google Authenticator y verifica el código",
            "data", response
        ));
    }

    @PostMapping("/2fa/verify")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Verificar y activar 2FA", description = "Verifica el código de Google Authenticator y activa 2FA")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> verify2FA(@RequestBody Verify2FARequestDTO request) {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
        
        if (usuarioOpt.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();

        if (usuario.getTotpSecret() == null || usuario.getTotpSecret().isEmpty()) {
            throw new BadRequestException("Primero debes configurar 2FA usando /api/v1/auth/2fa/setup");
        }
        
        boolean isValid = totpService.verifyCode(usuario.getTotpSecret(), request.getTotpCode());

        if (!isValid) {
            throw new BadRequestException("Código de autenticación inválido");
        }
        
        usuario.setTotpEnabled(true);
        usuarioService.guardar(usuario);
        
        List<String> backupCodes = backupCodeService.generateBackupCodes(userId);
        emailService.enviar2FAActivado(usuario.getEmail(), usuario.getNombreCompleto());

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "2FA activado exitosamente. GUARDA estos códigos de respaldo en un lugar seguro.");
        response.put("totpEnabled", true);
        response.put("backupCodes", backupCodes);
        response.put("totalCodes", backupCodes.size());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/2fa/disable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Deshabilitar 2FA", description = "Deshabilita 2FA después de verificar el código actual")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> disable2FA(@RequestBody Verify2FARequestDTO request) {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
        
        if (usuarioOpt.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (usuario.getTotpEnabled() == null || !usuario.getTotpEnabled()) {
            throw new BadRequestException("2FA no está habilitado");
        }
        
        boolean isValid = totpService.verifyCode(usuario.getTotpSecret(), request.getTotpCode());
        
        if (!isValid) {
            throw new BadRequestException("Código 2FA inválido. Necesitas el código correcto para deshabilitar 2FA");
        }
        
        usuario.setTotpEnabled(false);
        usuario.setTotpSecret(null);
        usuarioService.guardar(usuario);
        
        backupCodeService.deleteAllUserCodes(userId);
        trustedDeviceService.revokeAllUserDevices(userId);

        return ResponseEntity.ok(Map.of(
            "mensaje", "2FA deshabilitado exitosamente",
            "totpEnabled", false
        ));
    }

    @GetMapping("/2fa/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Estado de 2FA", description = "Verifica si el usuario tiene 2FA habilitado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> get2FAStatus() {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
        
        if (usuarioOpt.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        return ResponseEntity.ok(Map.of(
            "totpEnabled", usuario.getTotpEnabled() != null && usuario.getTotpEnabled(),
            "hasSecret", usuario.getTotpSecret() != null && !usuario.getTotpSecret().isEmpty()
        ));
    }

    // ENDPOINTS DE DISPOSITIVOS DE CONFIANZA

    @GetMapping("/trusted-devices")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Listar dispositivos de confianza", description = "Lista todos los dispositivos de confianza del usuario")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> listTrustedDevices() {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        List<TrustedDeviceDTO> devices = trustedDeviceService.listUserDevices(userId);
        
        return ResponseEntity.ok(Map.of(
            "devices", devices,
            "count", devices.size()
        ));
    }

    @DeleteMapping("/trusted-devices/{deviceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Revocar dispositivo de confianza", description = "Revoca un dispositivo de confianza específico")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> revokeTrustedDevice(@PathVariable Long deviceId) {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        trustedDeviceService.revokeDevice(deviceId, userId);
        
        return ResponseEntity.ok(Map.of("mensaje", "Dispositivo revocado exitosamente"));
    }

    @DeleteMapping("/trusted-devices")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Revocar todos los dispositivos", description = "Revoca todos los dispositivos de confianza del usuario")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> revokeAllTrustedDevices() {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        trustedDeviceService.revokeAllUserDevices(userId);
        
        return ResponseEntity.ok(Map.of("mensaje", "Todos los dispositivos revocados exitosamente"));
    }

    // ENDPOINTS DE RECUPERACIÓN DE CONTRASEÑA

    @PostMapping("/recuperar-contrasena")
    @Operation(summary = "Olvidé mi contraseña", description = "Envía un código de recuperación al email del usuario")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "mensaje", "Si el email existe, se enviará un código de recuperación"
            ));
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (usuario.getTotpEnabled() == null || !usuario.getTotpEnabled()) {
            throw new BadRequestException("Este usuario no tiene 2FA habilitado. Contacte al administrador.");
        }
        
        String recoveryCode = recoveryCodeService.generateRecoveryCode();
        String hashedCode = recoveryCodeService.hashCode(recoveryCode);
        
        usuario.setRecoveryCodeHash(hashedCode);
        usuario.setRecoveryCodeExpiry(recoveryCodeService.getExpiryTime());
        usuarioService.guardar(usuario);
        
        emailService.enviarCodigoRecuperacion(
            usuario.getEmail(),
            usuario.getNombreCompleto(),
            recoveryCode
        );

        return ResponseEntity.ok(Map.of(
            "mensaje", "Código de recuperación enviado a tu email. Válido por 10 minutos.",
            "expiresIn", recoveryCodeService.getExpiryMinutes() + " minutos"
        ));
    }

    @PostMapping("/restablecer-contrasena")
    @Operation(summary = "Resetear contraseña", description = "Resetea la contraseña usando código de recuperación + 2FA")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());
        
        if (usuarioOpt.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (usuario.getRecoveryCodeHash() == null || usuario.getRecoveryCodeExpiry() == null) {
            throw new BadRequestException("No hay código de recuperación activo. Solicita uno nuevo.");
        }
        
        if (recoveryCodeService.isExpired(usuario.getRecoveryCodeExpiry())) {
            usuario.setRecoveryCodeHash(null);
            usuario.setRecoveryCodeExpiry(null);
            usuarioService.guardar(usuario);
            throw new BadRequestException("Código de recuperación expirado. Solicita uno nuevo.");
        }
        
        boolean isRecoveryCodeValid = recoveryCodeService.verifyCode(
            request.getRecoveryCode(),
            usuario.getRecoveryCodeHash()
        );
        
        if (!isRecoveryCodeValid) {
            throw new BadRequestException("Código de recuperación inválido");
        }
        
        boolean is2FAValid = false;
        
        if (request.getTotpCode().length() == 6 && request.getTotpCode().matches("\\d+")) {
            is2FAValid = totpService.verifyCode(usuario.getTotpSecret(), request.getTotpCode());
        }
        
        if (!is2FAValid && request.getTotpCode().length() >= 12) {
            is2FAValid = backupCodeService.verifyAndUseBackupCode(usuario.getUsuarioID(), request.getTotpCode());
        }
        
        if (!is2FAValid) {
            throw new BadRequestException("Código de autenticación inválido");
        }

        usuarioService.cambiarContrasenia(usuario.getUsuarioID(), request.getNewPassword());
        
        usuario.setRecoveryCodeHash(null);
        usuario.setRecoveryCodeExpiry(null);
        usuarioService.guardar(usuario);
        
        trustedDeviceService.revokeAllUserDevices(usuario.getUsuarioID());

        return ResponseEntity.ok(Map.of(
            "mensaje", "Contraseña cambiada exitosamente. Por seguridad, todos tus dispositivos de confianza fueron revocados."
        ));
    }

    // ENDPOINTS DE CÓDIGOS DE RESPALDO

    @PostMapping("/2fa/backup-codes/regenerate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Regenerar códigos de respaldo", 
               description = "Genera un nuevo conjunto de códigos de respaldo, invalidando los anteriores. Requiere código TOTP para seguridad.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> regenerateBackupCodes(@RequestBody Verify2FARequestDTO request) {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
        
        if (usuarioOpt.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (usuario.getTotpEnabled() == null || !usuario.getTotpEnabled()) {
            throw new BadRequestException("2FA no está habilitado");
        }
        
        boolean isValid = totpService.verifyCode(usuario.getTotpSecret(), request.getTotpCode());
        
        if (!isValid) {
            throw new BadRequestException("Código 2FA inválido");
        }
        
        List<String> backupCodes = backupCodeService.regenerateBackupCodes(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Códigos de respaldo regenerados exitosamente. GUARDA estos nuevos códigos, los anteriores fueron invalidados.");
        response.put("backupCodes", backupCodes);
        response.put("totalCodes", backupCodes.size());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/2fa/backup-codes/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Contar códigos de respaldo disponibles", 
               description = "Obtiene la cantidad de códigos de respaldo disponibles (no usados)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getBackupCodesCount() {
        Integer userId = seguridadService.obtenerUsuarioAutenticado();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
        
        if (usuarioOpt.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (usuario.getTotpEnabled() == null || !usuario.getTotpEnabled()) {
            return ResponseEntity.ok(Map.of(
                "availableCodes", 0,
                "mensaje", "2FA no está habilitado"
            ));
        }
        
        long availableCodes = backupCodeService.getAvailableCodesCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("availableCodes", availableCodes);
        
        if (availableCodes <= 2) {
            response.put("warning", "Quedan pocos códigos de respaldo. Considera regenerarlos.");
        }
        
        return ResponseEntity.ok(response);
    }
}
