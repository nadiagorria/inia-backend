package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.util.List;


@Service
public class NotificationWebSocketService {

    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    
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

    
    public void sendToUsers(List<Integer> usuarioIds, NotificacionDTO notification) {
        usuarioIds.forEach(userId -> sendToUser(userId, notification));
        System.out.println(" Notificación enviada a " + usuarioIds.size() + " usuarios");
    }

    
    public void broadcastToRole(Rol rol, NotificacionDTO notification) {
        try {
            String destination = "/topic/notifications/" + rol.name().toLowerCase();
            messagingTemplate.convertAndSend(destination, notification);
            System.out.println(" Broadcast a rol " + rol.name() + ": " + notification.getNombre());
        } catch (Exception e) {
            System.err.println(" Error en broadcast a rol " + rol.name() + ": " + e.getMessage());
        }
    }

    
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
