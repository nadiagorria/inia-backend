package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.util.List;

/**
 * Servicio para enviar notificaciones en tiempo real via WebSocket
 * 
 * ¿Qué hace esta clase?
 * - Encapsula la lógica de emisión de mensajes WebSocket
 * - Permite enviar notificaciones a usuarios específicos o grupos
 * - Usa SimpMessagingTemplate (Simple Messaging Template) de Spring
 * 
 * ¿Qué es SimpMessagingTemplate?
 * Es una clase de Spring que facilita el envío de mensajes a través de WebSocket.
 * Piensa en ella como un "servicio postal" que entrega mensajes a las direcciones correctas.
 * 
 * Tipos de envío:
 * 1. sendToUser() - Enviar a UN usuario específico (privado)
 * 2. convertAndSend() - Broadcast a un canal (múltiples usuarios)
 * 3. broadcastToRole() - Enviar a todos los usuarios con un rol específico
 * 
 * Estructura de canales:
 * - /user/{userId}/queue/notifications - Notificaciones privadas de un usuario
 * - /user/{userId}/queue/notifications/count - Contador de no leídas
 * - /topic/notifications/{rol} - Broadcast a un rol específico
 * - /topic/notifications/all - Broadcast a todos
 */
@Service
public class NotificationWebSocketService {

    /**
     * Template de Spring para enviar mensajes WebSocket
     * Se inyecta automáticamente por Spring
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Enviar notificación a un usuario específico
     * 
     * Ejemplo de uso:
     * sendToUser(123, nuevaNotificacion);
     * 
     * El usuario con ID 123 recibirá la notificación en:
     * /user/123/queue/notifications
     * 
     * @param usuarioId ID del usuario destinatario
     * @param notification Objeto NotificacionDTO a enviar
     */
    public void sendToUser(Integer usuarioId, NotificacionDTO notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                usuarioId.toString(),           // Destinatario (userId)
                "/queue/notifications",         // Canal destino
                notification                     // Payload (datos)
            );
            System.out.println(" Notificación enviada a usuario " + usuarioId + ": " + notification.getNombre());
        } catch (Exception e) {
            System.err.println(" Error enviando notificación WebSocket a usuario " + usuarioId + ": " + e.getMessage());
        }
    }

    /**
     * Enviar notificación a múltiples usuarios
     * 
     * Ejemplo de uso:
     * List<Integer> adminIds = Arrays.asList(1, 2, 3);
     * sendToUsers(adminIds, nuevaNotificacion);
     * 
     * Cada admin recibirá la notificación individualmente
     * 
     * @param usuarioIds Lista de IDs de usuarios
     * @param notification Notificación a enviar
     */
    public void sendToUsers(List<Integer> usuarioIds, NotificacionDTO notification) {
        usuarioIds.forEach(userId -> sendToUser(userId, notification));
        System.out.println(" Notificación enviada a " + usuarioIds.size() + " usuarios");
    }

    /**
     * Broadcast a todos los usuarios con un rol específico
     * 
     * Ejemplo de uso:
     * broadcastToRole(Rol.ADMIN, nuevaNotificacion);
     * 
     * IMPORTANTE: Los clientes deben suscribirse al canal:
     * /topic/notifications/admin (para admins)
     * /topic/notifications/analista (para analistas)
     * 
     * ¿Cuándo usar esto vs sendToUser?
     * - sendToUser: Cuando sabes exactamente los IDs de usuarios
     * - broadcastToRole: Cuando quieres que TODOS los que tienen un rol lo reciban
     *                    sin necesidad de buscar sus IDs
     * 
     * @param rol Rol objetivo (ADMIN, ANALISTA, OBSERVADOR)
     * @param notification Notificación a enviar
     */
    public void broadcastToRole(Rol rol, NotificacionDTO notification) {
        try {
            String destination = "/topic/notifications/" + rol.name().toLowerCase();
            messagingTemplate.convertAndSend(destination, notification);
            System.out.println(" Broadcast a rol " + rol.name() + ": " + notification.getNombre());
        } catch (Exception e) {
            System.err.println(" Error en broadcast a rol " + rol.name() + ": " + e.getMessage());
        }
    }

    /**
     * Broadcast general a TODOS los usuarios conectados
     * 
     * Ejemplo de uso:
     * broadcast(mantenimientoNotificacion);
     * 
     * Útil para:
     * - Notificaciones de mantenimiento del sistema
     * - Anuncios generales
     * - Actualizaciones importantes
     * 
     * @param notification Notificación a enviar a todos
     */
    public void broadcast(NotificacionDTO notification) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/notifications/all",
                notification
            );
            System.out.println(" Broadcast global: " + notification.getNombre());
        } catch (Exception e) {
            System.err.println(" Error en broadcast global: " + e.getMessage());
        }
    }

    /**
     * Enviar contador de notificaciones no leídas actualizado
     * 
     * Ejemplo de uso:
     * sendUnreadCount(123, 5L); 
     * 
     * El frontend recibirá solo el número (sin necesidad de hacer GET)
     * y actualizará el badge instantáneamente
     * 
     * @param usuarioId ID del usuario
     * @param count Número de notificaciones no leídas
     */
    public void sendUnreadCount(Integer usuarioId, Long count) {
        try {
            messagingTemplate.convertAndSendToUser(
                usuarioId.toString(),
                "/queue/notifications/count",
                count
            );
            System.out.println(" Contador actualizado para usuario " + usuarioId + ": " + count);
        } catch (Exception e) {
            System.err.println(" Error enviando contador a usuario " + usuarioId + ": " + e.getMessage());
        }
    }

    /**
     * Enviar evento de notificación marcada como leída
     * 
     * Útil para sincronizar múltiples dispositivos/pestañas del mismo usuario
     * 
     * @param usuarioId ID del usuario
     * @param notificacionId ID de la notificación marcada como leída
     */
    public void sendMarkAsRead(Integer usuarioId, Long notificacionId) {
        try {
            messagingTemplate.convertAndSendToUser(
                usuarioId.toString(),
                "/queue/notifications/mark-read",
                notificacionId
            );
            System.out.println(" Notificación " + notificacionId + " marcada como leída para usuario " + usuarioId);
        } catch (Exception e) {
            System.err.println(" Error enviando mark-read: " + e.getMessage());
        }
    }

    /**
     * Enviar evento de notificación eliminada
     * 
     * @param usuarioId ID del usuario
     * @param notificacionId ID de la notificación eliminada
     */
    public void sendDeleted(Integer usuarioId, Long notificacionId) {
        try {
            messagingTemplate.convertAndSendToUser(
                usuarioId.toString(),
                "/queue/notifications/deleted",
                notificacionId
            );
            System.out.println(" Notificación " + notificacionId + " eliminada para usuario " + usuarioId);
        } catch (Exception e) {
            System.err.println(" Error enviando deleted: " + e.getMessage());
        }
    }
}
