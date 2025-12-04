package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.NotificacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.NotificacionService;


@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Gestión de notificaciones del sistema")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear notificación manual", description = "Crear una notificación manual (solo administradores)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificacionDTO> crearNotificacion(@RequestBody NotificacionRequestDTO request) {
        NotificacionDTO notificacion = notificacionService.crearNotificacion(request);
        return ResponseEntity.ok(notificacion);
    }

    
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener notificaciones por usuario", description = "Obtener notificaciones de un usuario con paginación (solo admins pueden ver otras notificaciones)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<NotificacionDTO>> obtenerNotificacionesPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesPorUsuarioConValidacion(usuarioId, pageable);
        return ResponseEntity.ok(notificaciones);
    }

    
    @GetMapping("/mis-notificaciones")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener mis notificaciones", description = "Obtener notificaciones del usuario autenticado con paginación")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<NotificacionDTO>> obtenerMisNotificaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificacionDTO> notificaciones = notificacionService.obtenerMisNotificaciones(pageable);
        return ResponseEntity.ok(notificaciones);
    }

    
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener notificaciones no leídas", description = "Obtener notificaciones no leídas de un usuario (solo admins pueden ver otras notificaciones)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<NotificacionDTO>> obtenerNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        List<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesNoLeidasConValidacion(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    
    @GetMapping("/mis-notificaciones/no-leidas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener mis notificaciones no leídas", description = "Obtener notificaciones no leídas del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<NotificacionDTO>> obtenerMisNotificacionesNoLeidas() {
        List<NotificacionDTO> notificaciones = notificacionService.obtenerMisNotificacionesNoLeidas();
        return ResponseEntity.ok(notificaciones);
    }

    
    @GetMapping("/usuario/{usuarioId}/contar-no-leidas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Contar notificaciones no leídas", description = "Contar el número de notificaciones no leídas de un usuario (solo admins pueden ver otras notificaciones)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Long> contarNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        Long count = notificacionService.contarNotificacionesNoLeidasConValidacion(usuarioId);
        return ResponseEntity.ok(count);
    }

    
    @GetMapping("/mis-notificaciones/contar-no-leidas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Contar mis notificaciones no leídas", description = "Contar el número de notificaciones no leídas del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Long> contarMisNotificacionesNoLeidas() {
        Long count = notificacionService.contarMisNotificacionesNoLeidas();
        return ResponseEntity.ok(count);
    }

    
    @PutMapping("/{notificacionId}/marcar-leida")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Marcar notificación como leída", description = "Marcar una notificación específica como leída")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificacionDTO> marcarComoLeida(@PathVariable Long notificacionId) {
        NotificacionDTO notificacion = notificacionService.marcarComoLeida(notificacionId);
        return ResponseEntity.ok(notificacion);
    }

    
    @PutMapping("/usuario/{usuarioId}/marcar-todas-leidas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Marcar todas como leídas", description = "Marcar todas las notificaciones de un usuario como leídas (solo admins pueden marcar otras notificaciones)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> marcarTodasComoLeidas(@PathVariable Long usuarioId) {
        notificacionService.marcarTodasComoLeidasConValidacion(usuarioId);
        return ResponseEntity.ok().build();
    }

    
    @PutMapping("/mis-notificaciones/marcar-todas-leidas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Marcar todas mis notificaciones como leídas", description = "Marcar todas las notificaciones del usuario autenticado como leídas")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> marcarTodasMisNotificacionesComoLeidas() {
        notificacionService.marcarTodasMisNotificacionesComoLeidas();
        return ResponseEntity.ok().build();
    }

    
    @DeleteMapping("/{notificacionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Eliminar notificación", description = "Eliminar una notificación (marcar como inactiva)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long notificacionId) {
        notificacionService.eliminarNotificacion(notificacionId);
        return ResponseEntity.ok().build();
    }
}