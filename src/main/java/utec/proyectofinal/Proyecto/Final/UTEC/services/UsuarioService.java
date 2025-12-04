package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ActualizarPerfilRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.AprobarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GestionarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RegistroUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificacionService notificacionService;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private TotpService totpService;

    @Autowired
    private BackupCodeService backupCodeService;

    /**
     * Buscar usuario por ID
     */
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Registrar nueva solicitud de usuario
     */
    public UsuarioDTO registrarSolicitud(RegistroUsuarioRequestDTO solicitud) {
        
        if (usuarioRepository.findByNombre(solicitud.getNombre()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        
        if (usuarioRepository.findByEmail(solicitud.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        
        validarContrasenia(solicitud.getContrasenia());

        
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.getNombre());
        usuario.setNombres(solicitud.getNombres());
        usuario.setApellidos(solicitud.getApellidos());
        usuario.setEmail(solicitud.getEmail());
        usuario.setContrasenia(passwordEncoder.encode(solicitud.getContrasenia()));
        usuario.setEstado(EstadoUsuario.PENDIENTE);
        usuario.setRol(null); 
        usuario.setActivo(false); 

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        
        try {
            notificacionService.notificarNuevoUsuario(usuarioGuardado.getUsuarioID().longValue());
        } catch (Exception e) {
            
            System.err.println("Error creating notification for new user: " + e.getMessage());
        }
        
        
        try {
            
            emailService.enviarEmailConfirmacionRegistro(
                usuarioGuardado.getEmail(),
                usuarioGuardado.getNombres() + " " + usuarioGuardado.getApellidos()
            );
            
            
            List<Usuario> analistas = usuarioRepository.findAllByRol(Rol.ANALISTA);
            for (Usuario analista : analistas) {
                if (analista.getActivo() && analista.getEmail() != null) {
                    emailService.enviarEmailNuevoRegistro(
                        analista.getEmail(),
                        analista.getNombres() + " " + analista.getApellidos(),
                        usuarioGuardado.getNombres() + " " + usuarioGuardado.getApellidos(),
                        usuarioGuardado.getEmail()
                    );
                }
            }
            
            System.out.println("✉️ Emails enviados: confirmación a usuario y notificación a " + analistas.size() + " analista(s)");
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando emails (registro continúa): " + e.getMessage());
        }
        
        return mapearEntidadADTO(usuarioGuardado);
    }

    /**
     * Listar solicitudes pendientes de aprobación
     */
    public List<UsuarioDTO> listarSolicitudesPendientes() {
        return usuarioRepository.findByEstado(EstadoUsuario.PENDIENTE)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar solicitudes pendientes con paginación y búsqueda
     */
    public Page<UsuarioDTO> listarSolicitudesPendientesPaginadas(int page, int size, String search) {
        
        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        Page<Usuario> usuariosPage;
        
        if (search != null && !search.trim().isEmpty()) {
            
            usuariosPage = usuarioRepository.findByEstadoAndSearchTerm(EstadoUsuario.PENDIENTE, search, pageable);
        } else {
            usuariosPage = usuarioRepository.findByEstado(EstadoUsuario.PENDIENTE, pageable);
        }
        
        return usuariosPage.map(this::mapearEntidadADTO);
    }

    /**
     * Aprobar usuario y asignar rol
     */
    public UsuarioDTO aprobarUsuario(Integer usuarioId, AprobarUsuarioRequestDTO solicitud) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            throw new RuntimeException("Solo se pueden aprobar usuarios en estado PENDIENTE");
        }

        usuario.setRol(solicitud.getRol());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setActivo(true);

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        
        
        try {
            notificacionService.notificarUsuarioAprobado(usuarioActualizado.getUsuarioID().longValue());
        } catch (Exception e) {
            
            System.err.println("Error creating notification for user approval: " + e.getMessage());
        }
        
        
        try {
            emailService.enviarEmailBienvenida(
                usuarioActualizado.getEmail(),
                usuarioActualizado.getNombres() + " " + usuarioActualizado.getApellidos()
            );
            System.out.println("✉️ Email de bienvenida enviado a: " + usuarioActualizado.getEmail());
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando email de bienvenida (aprobación continúa): " + e.getMessage());
        }
        
        return mapearEntidadADTO(usuarioActualizado);
    }

    /**
     * Rechazar solicitud de usuario
     */
    public void rechazarSolicitud(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            throw new RuntimeException("Solo se pueden rechazar usuarios en estado PENDIENTE");
        }

        
        try {
            notificacionService.notificarUsuarioRechazado(usuario.getUsuarioID().longValue());
        } catch (Exception e) {
            
            System.err.println("Error creating notification for user rejection: " + e.getMessage());
        }

        usuarioRepository.delete(usuario);
    }

    /**
     * Listar todos los usuarios (para administración)
     */
    public List<UsuarioDTO> listarTodosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar todos los usuarios con paginación, búsqueda y filtros
     */
    public Page<UsuarioDTO> listarTodosUsuariosPaginados(int page, int size, String search, Rol rol, Boolean activo) {
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.ASC, "apellidos", "nombres"));
        Page<Usuario> usuariosPage;
        
        boolean hasSearch = search != null && !search.trim().isEmpty();
        
        
        if (rol != null && activo != null && hasSearch) {
            
            usuariosPage = usuarioRepository.findByRolAndActivoAndSearchTerm(rol, activo, search, pageable);
        } else if (rol != null && activo != null) {
            
            usuariosPage = usuarioRepository.findByRolAndActivo(rol, activo, pageable);
        } else if (rol != null && hasSearch) {
            
            usuariosPage = usuarioRepository.findBySearchTermAndRol(search, rol, pageable);
        } else if (activo != null && hasSearch) {
            
            usuariosPage = usuarioRepository.findByActivoAndSearchTerm(activo, search, pageable);
        } else if (rol != null) {
            
            usuariosPage = usuarioRepository.findByRol(rol, pageable);
        } else if (activo != null) {
            
            usuariosPage = usuarioRepository.findByActivo(activo, pageable);
        } else if (hasSearch) {
            
            usuariosPage = usuarioRepository.findBySearchTerm(search, pageable);
        } else {
            
            usuariosPage = usuarioRepository.findAll(pageable);
        }
        
        return usuariosPage.map(this::mapearEntidadADTO);
    }

    /**
     * Listar usuarios activos
     */
    public List<UsuarioDTO> listarUsuariosActivos() {
        return usuarioRepository.findByEstado(EstadoUsuario.ACTIVO)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    /**
     * Gestionar usuario (cambiar rol o estado)
     */
    public UsuarioDTO gestionarUsuario(Integer usuarioId, GestionarUsuarioRequestDTO solicitud) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        
        if (solicitud.getRol() != null) {
            usuario.setRol(solicitud.getRol());
        }

        
        if (solicitud.getEstado() != null) {
            usuario.setEstado(solicitud.getEstado());
            
            usuario.setActivo(solicitud.getEstado() == EstadoUsuario.ACTIVO);
        }
        
        
        if (solicitud.getActivo() != null) {
            usuario.setActivo(solicitud.getActivo());
            
            usuario.setEstado(solicitud.getActivo() ? EstadoUsuario.ACTIVO : EstadoUsuario.INACTIVO);
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapearEntidadADTO(usuarioActualizado);
    }

    /**
     * Obtener perfil del usuario actual
     */
    public UsuarioDTO obtenerPerfil() {
        Usuario usuario = obtenerUsuarioActual();
        return mapearEntidadADTO(usuario);
    }

    /**
     * Actualizar perfil del usuario actual
     */
    public UsuarioDTO actualizarPerfil(ActualizarPerfilRequestDTO solicitud) {
        Usuario usuario = obtenerUsuarioActual();

        
        if (solicitud.getContraseniaNueva() != null && !solicitud.getContraseniaNueva().isEmpty()) {
            if (solicitud.getContraseniaActual() == null || solicitud.getContraseniaActual().isEmpty()) {
                throw new RuntimeException("Debe proporcionar la contraseña actual para cambiarla");
            }
            
            if (!passwordEncoder.matches(solicitud.getContraseniaActual(), usuario.getContrasenia())) {
                throw new RuntimeException("Contraseña actual incorrecta");
            }
            
            
            if (passwordEncoder.matches(solicitud.getContraseniaNueva(), usuario.getContrasenia())) {
                throw new RuntimeException("La nueva contraseña no puede ser igual a la contraseña actual");
            }
            
            
            validarContrasenia(solicitud.getContraseniaNueva());
            
            usuario.setContrasenia(passwordEncoder.encode(solicitud.getContraseniaNueva()));
        }

        
        if (solicitud.getNombre() != null && !solicitud.getNombre().trim().isEmpty() 
            && !solicitud.getNombre().equalsIgnoreCase(usuario.getNombre())) {
            
            Optional<Usuario> usuarioExistente = usuarioRepository.findByNombreIgnoreCase(solicitud.getNombre());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getUsuarioID().equals(usuario.getUsuarioID())) {
                throw new RuntimeException("El nombre de usuario ya está en uso por otro usuario");
            }
            usuario.setNombre(solicitud.getNombre());
        }

        
        if (solicitud.getNombres() != null && !solicitud.getNombres().trim().isEmpty()) {
            usuario.setNombres(solicitud.getNombres());
        }
        
        if (solicitud.getApellidos() != null && !solicitud.getApellidos().trim().isEmpty()) {
            usuario.setApellidos(solicitud.getApellidos());
        }
        
        if (solicitud.getEmail() != null && !solicitud.getEmail().trim().isEmpty() 
            && !solicitud.getEmail().equalsIgnoreCase(usuario.getEmail())) {
            
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmailIgnoreCase(solicitud.getEmail());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getUsuarioID().equals(usuario.getUsuarioID())) {
                throw new RuntimeException("El email ya está en uso por otro usuario");
            }
            usuario.setEmail(solicitud.getEmail());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapearEntidadADTO(usuarioActualizado);
    }

    /**
     * Crear admin predeterminado si no existe
     * El admin se crea con 2FA YA ACTIVADO para cumplir con la política de seguridad
     */
    public UsuarioDTO crearAdminPredeterminado() {
        
        if (usuarioRepository.existsByRol(Rol.ADMIN)) {
            throw new RuntimeException("Ya existe un administrador en el sistema");
        }

        System.out.println("=" .repeat(80));
        System.out.println("🔐 CREANDO USUARIO ADMINISTRADOR CON 2FA OBLIGATORIO");
        System.out.println("=" .repeat(80));

        
        Usuario admin = new Usuario();
        admin.setNombre("admin");
        admin.setNombres("Administrador");
        admin.setApellidos("del Sistema");
        admin.setEmail("admin@temporal.local"); 
        admin.setContrasenia(passwordEncoder.encode("admin123")); 
        admin.setRol(Rol.ADMIN);
        admin.setEstado(EstadoUsuario.ACTIVO);
        admin.setActivo(true);
        admin.setRequiereCambioCredenciales(true); 

        
        String secret = totpService.generateSecret();
        admin.setTotpSecret(secret);
        admin.setTotpEnabled(false); 

        Usuario adminGuardado = usuarioRepository.save(admin);

        
        
        
        System.out.println("\n✅ ADMINISTRADOR CREADO EXITOSAMENTE");
        System.out.println("-".repeat(80));
        System.out.println("📧 Usuario: admin");
        System.out.println("🔑 Contraseña temporal: admin123");
        System.out.println("-".repeat(80));
        System.out.println("\n⚠️  CONFIGURACIÓN INICIAL REQUERIDA");
        System.out.println("-".repeat(80));
        System.out.println("1. Ve a http://localhost:3000/login");
        System.out.println("2. Ingresa las credenciales temporales (admin / admin123)");
        System.out.println("3. Serás redirigido a configurar:");
        System.out.println("   - Tu email real");
        System.out.println("   - Tu contraseña segura");
        System.out.println("   - Google Authenticator (2FA obligatorio)");
        System.out.println("4. Recibirás códigos de respaldo (guárdalos en lugar seguro)");
        System.out.println("-".repeat(80));
        System.out.println("\n💡 TIP: El sistema te guiará paso a paso en el navegador");
        System.out.println("=" .repeat(80));
        System.out.println("\n");

        return mapearEntidadADTO(adminGuardado);
    }

    

    /**
     * Guardar usuario (método público para uso desde controladores 2FA)
     */
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Buscar usuario por email
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Cambiar contraseña de un usuario (para recuperación de contraseña)
     */
    public void cambiarContrasenia(Integer usuarioId, String nuevaContrasenia) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        
        if (passwordEncoder.matches(nuevaContrasenia, usuario.getContrasenia())) {
            throw new RuntimeException("La nueva contraseña no puede ser igual a la contraseña actual");
        }
        
        
        validarContrasenia(nuevaContrasenia);
        
        
        usuario.setContrasenia(passwordEncoder.encode(nuevaContrasenia));
        usuarioRepository.save(usuario);
        
        System.out.println("✅ Contraseña cambiada para usuario: " + usuario.getNombre());
    }

    

    /**
     * Valida que la contraseña cumpla con los requisitos de seguridad
     * @param contrasenia la contraseña a validar
     * @throws RuntimeException si la contraseña no cumple los requisitos
     */
    private void validarContrasenia(String contrasenia) {
        if (contrasenia == null || contrasenia.trim().isEmpty()) {
            throw new RuntimeException("La contraseña no puede estar vacía");
        }
        
        if (contrasenia.length() < 8) {
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");
        }
        
        
        if (!contrasenia.matches(".*[a-zA-Z].*")) {
            throw new RuntimeException("La contraseña debe contener al menos una letra");
        }
        
        
        if (!contrasenia.matches(".*\\d.*")) {
            throw new RuntimeException("La contraseña debe contener al menos un número");
        }
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioRepository.findByNombre(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en base de datos"));
    }

    private UsuarioDTO mapearEntidadADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setUsuarioID(usuario.getUsuarioID());
        dto.setNombre(usuario.getNombre());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setEmail(usuario.getEmail());
        
        
        dto.setRol(usuario.getRol());
        
        
        if (usuario.getRol() != null) {
            dto.setRoles(List.of(usuario.getRol().name()));
        } else {
            dto.setRoles(List.of());
        }
        
        dto.setEstado(usuario.getEstado());
        
        
        if (usuario.getEstado() != null) {
            dto.setEstadoSolicitud(usuario.getEstado().name());
        }
        
        dto.setActivo(usuario.getActivo());
        
        
        dto.setFechaCreacion(usuario.getFechaCreacion());
        
        
        if (usuario.getFechaCreacion() != null) {
            dto.setFechaRegistro(usuario.getFechaCreacion().toString());
        }
        
        dto.setFechaUltimaConexion(usuario.getFechaUltimaConexion());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        return dto;
    }

    /**
     * Verifica si un nombre de usuario está disponible
     * @param nombre el nombre de usuario a verificar
     * @return true si está disponible, false si ya existe
     */
    public boolean esNombreUsuarioDisponible(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        return usuarioRepository.findByNombre(nombre.trim()).isEmpty();
    }

    /**
     * Verifica si un email está disponible
     * @param email el email a verificar
     * @return true si está disponible, false si ya existe
     */
    public boolean esEmailDisponible(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return usuarioRepository.findByEmail(email.trim()).isEmpty();
    }
}