package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para NotificationWebSocketService
 * 
 * Funcionalidades testeadas:
 * - Envío de notificaciones a usuario específico
 * - Envío de notificaciones a múltiples usuarios
 * - Broadcast a rol específico (ADMIN, ANALISTA, OBSERVADOR)
 * - Broadcast global a todos los usuarios
 * - Envío de contador de notificaciones no leídas
 * - Envío de notificación marcada como leída
 * - Envío de notificación eliminada
 * - Manejo de errores en envíos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de NotificationWebSocketService")
class NotificationWebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationWebSocketService service;

    private NotificacionDTO notificacion;

    @BeforeEach
    void setUp() {
        
        notificacion = new NotificacionDTO();
        notificacion.setId(1L);
        notificacion.setNombre("Análisis Aprobado");
        notificacion.setMensaje("El análisis de germinación ha sido aprobado");
        notificacion.setLeido(false);
        notificacion.setActivo(true);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setUsuarioId(123L);
        notificacion.setUsuarioNombre("Juan Pérez");
        notificacion.setAnalisisId(456L);
        notificacion.setTipo(TipoNotificacion.ANALISIS_APROBADO);
    }

    @Test
    @DisplayName("sendToUser - debe enviar notificación a usuario específico")
    void sendToUser_debeEnviarNotificacionAUsuario() {
        
        Integer usuarioId = 123;
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NotificacionDTO> notificationCaptor = ArgumentCaptor.forClass(NotificacionDTO.class);

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.sendToUser(usuarioId, notificacion);

        
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                userCaptor.capture(),
                destinationCaptor.capture(),
                notificationCaptor.capture()
        );

        assertEquals("123", userCaptor.getValue(), "El ID de usuario debe ser '123'");
        assertEquals("/queue/notifications", destinationCaptor.getValue(), 
                "El destino debe ser '/queue/notifications'");
        assertEquals(notificacion, notificationCaptor.getValue(), 
                "La notificación debe ser la misma");
    }

    @Test
    @DisplayName("sendToUser - debe enviar notificación con todos los campos correctos")
    void sendToUser_debeEnviarNotificacionConTodosLosCampos() {
        
        Integer usuarioId = 456;
        ArgumentCaptor<NotificacionDTO> notificationCaptor = ArgumentCaptor.forClass(NotificacionDTO.class);

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.sendToUser(usuarioId, notificacion);

        
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("456"),
                eq("/queue/notifications"),
                notificationCaptor.capture()
        );

        NotificacionDTO capturedNotification = notificationCaptor.getValue();
        assertEquals(1L, capturedNotification.getId(), "El ID debe ser 1");
        assertEquals("Análisis Aprobado", capturedNotification.getNombre(), "El nombre debe coincidir");
        assertEquals("El análisis de germinación ha sido aprobado", capturedNotification.getMensaje(), 
                "El mensaje debe coincidir");
        assertFalse(capturedNotification.getLeido(), "Debe estar marcada como no leída");
        assertEquals(TipoNotificacion.ANALISIS_APROBADO, capturedNotification.getTipo(), 
                "El tipo debe ser ANALISIS_APROBADO");
    }

    @Test
    @DisplayName("sendToUser - debe manejar errores sin lanzar excepción")
    void sendToUser_debeManejarlErroresSinLanzarExcepcion() {
        
        Integer usuarioId = 789;
        doThrow(new RuntimeException("Error de conexión WebSocket"))
                .when(messagingTemplate).convertAndSendToUser(
                        anyString(),
                        anyString(),
                        any(NotificacionDTO.class)
                );

        
        assertDoesNotThrow(() -> service.sendToUser(usuarioId, notificacion),
                "No debe lanzar excepción cuando hay error en el envío");

        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("789"),
                eq("/queue/notifications"),
                eq(notificacion)
        );
    }

    @Test
    @DisplayName("sendToUsers - debe enviar notificación a múltiples usuarios")
    void sendToUsers_debeEnviarAMultiplesUsuarios() {
        
        List<Integer> usuarioIds = Arrays.asList(1, 2, 3, 4, 5);

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.sendToUsers(usuarioIds, notificacion);

        
        verify(messagingTemplate, times(5)).convertAndSendToUser(
                anyString(),
                eq("/queue/notifications"),
                eq(notificacion)
        );

        
        verify(messagingTemplate).convertAndSendToUser("1", "/queue/notifications", notificacion);
        verify(messagingTemplate).convertAndSendToUser("2", "/queue/notifications", notificacion);
        verify(messagingTemplate).convertAndSendToUser("3", "/queue/notifications", notificacion);
        verify(messagingTemplate).convertAndSendToUser("4", "/queue/notifications", notificacion);
        verify(messagingTemplate).convertAndSendToUser("5", "/queue/notifications", notificacion);
    }

    @Test
    @DisplayName("sendToUsers - debe enviar a un solo usuario si la lista tiene uno")
    void sendToUsers_debeEnviarAUnSoloUsuario() {
        
        List<Integer> usuarioIds = Arrays.asList(100);

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.sendToUsers(usuarioIds, notificacion);

        
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("100"),
                eq("/queue/notifications"),
                eq(notificacion)
        );
    }

    @Test
    @DisplayName("sendToUsers - no debe enviar nada si la lista está vacía")
    void sendToUsers_noDebeEnviarSiListaVacia() {
        
        List<Integer> usuarioIds = Arrays.asList();

        
        service.sendToUsers(usuarioIds, notificacion);

        
        verify(messagingTemplate, never()).convertAndSendToUser(
                anyString(),
                anyString(),
                any(NotificacionDTO.class)
        );
    }

    @Test
    @DisplayName("broadcastToRole - debe enviar a rol ADMIN")
    void broadcastToRole_debeEnviarARolAdmin() {
        
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NotificacionDTO> notificationCaptor = ArgumentCaptor.forClass(NotificacionDTO.class);

        doNothing().when(messagingTemplate).convertAndSend(
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.broadcastToRole(Rol.ADMIN, notificacion);

        
        verify(messagingTemplate, times(1)).convertAndSend(
                destinationCaptor.capture(),
                notificationCaptor.capture()
        );

        assertEquals("/topic/notifications/admin", destinationCaptor.getValue(),
                "El destino debe ser '/topic/notifications/admin'");
        assertEquals(notificacion, notificationCaptor.getValue(),
                "La notificación debe ser la misma");
    }

    @Test
    @DisplayName("broadcastToRole - debe enviar a rol ANALISTA")
    void broadcastToRole_debeEnviarARolAnalista() {
        
        doNothing().when(messagingTemplate).convertAndSend(
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.broadcastToRole(Rol.ANALISTA, notificacion);

        
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/analista"),
                eq(notificacion)
        );
    }

    @Test
    @DisplayName("broadcastToRole - debe enviar a rol OBSERVADOR")
    void broadcastToRole_debeEnviarARolObservador() {
        
        doNothing().when(messagingTemplate).convertAndSend(
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.broadcastToRole(Rol.OBSERVADOR, notificacion);

        
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/observador"),
                eq(notificacion)
        );
    }

    @Test
    @DisplayName("broadcastToRole - debe manejar errores sin lanzar excepción")
    void broadcastToRole_debeManejarlErroresSinLanzarExcepcion() {
        
        doThrow(new RuntimeException("Error en broadcast"))
                .when(messagingTemplate).convertAndSend(
                        anyString(),
                        any(NotificacionDTO.class)
                );

        
        assertDoesNotThrow(() -> service.broadcastToRole(Rol.ADMIN, notificacion),
                "No debe lanzar excepción cuando hay error en el broadcast");

        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/admin"),
                eq(notificacion)
        );
    }

    @Test
    @DisplayName("broadcast - debe enviar notificación global a todos")
    void broadcast_debeEnviarGlobalATodos() {
        
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NotificacionDTO> notificationCaptor = ArgumentCaptor.forClass(NotificacionDTO.class);

        doNothing().when(messagingTemplate).convertAndSend(
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.broadcast(notificacion);

        
        verify(messagingTemplate, times(1)).convertAndSend(
                destinationCaptor.capture(),
                notificationCaptor.capture()
        );

        assertEquals("/topic/notifications/all", destinationCaptor.getValue(),
                "El destino debe ser '/topic/notifications/all'");
        assertEquals(notificacion, notificationCaptor.getValue(),
                "La notificación debe ser la misma");
    }

    @Test
    @DisplayName("broadcast - debe manejar errores sin lanzar excepción")
    void broadcast_debeManejarlErroresSinLanzarExcepcion() {
        
        doThrow(new RuntimeException("Error en broadcast global"))
                .when(messagingTemplate).convertAndSend(
                        anyString(),
                        any(NotificacionDTO.class)
                );

        
        assertDoesNotThrow(() -> service.broadcast(notificacion),
                "No debe lanzar excepción cuando hay error en el broadcast global");

        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/all"),
                eq(notificacion)
        );
    }

    @Test
    @DisplayName("sendUnreadCount - debe enviar contador de no leídas a usuario")
    void sendUnreadCount_debeEnviarContador() {
        
        Integer usuarioId = 123;
        Long count = 5L;
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> countCaptor = ArgumentCaptor.forClass(Long.class);

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(Long.class)
        );

        
        service.sendUnreadCount(usuarioId, count);

        
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                userCaptor.capture(),
                destinationCaptor.capture(),
                countCaptor.capture()
        );

        assertEquals("123", userCaptor.getValue(), "El ID de usuario debe ser '123'");
        assertEquals("/queue/notifications/count", destinationCaptor.getValue(),
                "El destino debe ser '/queue/notifications/count'");
        assertEquals(5L, countCaptor.getValue(), "El contador debe ser 5");
    }

    @Test
    @DisplayName("sendUnreadCount - debe enviar contador cero")
    void sendUnreadCount_debeEnviarContadorCero() {
        
        Integer usuarioId = 456;
        Long count = 0L;

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(Long.class)
        );

        
        service.sendUnreadCount(usuarioId, count);

        
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("456"),
                eq("/queue/notifications/count"),
                eq(0L)
        );
    }

    @Test
    @DisplayName("sendUnreadCount - debe manejar errores sin lanzar excepción")
    void sendUnreadCount_debeManejarlErroresSinLanzarExcepcion() {
        
        Integer usuarioId = 789;
        Long count = 10L;

        doThrow(new RuntimeException("Error enviando contador"))
                .when(messagingTemplate).convertAndSendToUser(
                        anyString(),
                        anyString(),
                        any(Long.class)
                );

        
        assertDoesNotThrow(() -> service.sendUnreadCount(usuarioId, count),
                "No debe lanzar excepción cuando hay error enviando el contador");

        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("789"),
                eq("/queue/notifications/count"),
                eq(10L)
        );
    }

    @Test
    @DisplayName("sendMarkAsRead - debe enviar evento de marcado como leído")
    void sendMarkAsRead_debeEnviarEvento() {
        
        Integer usuarioId = 123;
        Long notificacionId = 456L;
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(Long.class)
        );

        
        service.sendMarkAsRead(usuarioId, notificacionId);

        
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                userCaptor.capture(),
                destinationCaptor.capture(),
                idCaptor.capture()
        );

        assertEquals("123", userCaptor.getValue(), "El ID de usuario debe ser '123'");
        assertEquals("/queue/notifications/mark-read", destinationCaptor.getValue(),
                "El destino debe ser '/queue/notifications/mark-read'");
        assertEquals(456L, idCaptor.getValue(), "El ID de notificación debe ser 456");
    }

    @Test
    @DisplayName("sendMarkAsRead - debe manejar errores sin lanzar excepción")
    void sendMarkAsRead_debeManejarlErroresSinLanzarExcepcion() {
        
        Integer usuarioId = 789;
        Long notificacionId = 999L;

        doThrow(new RuntimeException("Error enviando mark-read"))
                .when(messagingTemplate).convertAndSendToUser(
                        anyString(),
                        anyString(),
                        any(Long.class)
                );

        
        assertDoesNotThrow(() -> service.sendMarkAsRead(usuarioId, notificacionId),
                "No debe lanzar excepción cuando hay error enviando mark-read");

        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("789"),
                eq("/queue/notifications/mark-read"),
                eq(999L)
        );
    }

    @Test
    @DisplayName("sendDeleted - debe enviar evento de notificación eliminada")
    void sendDeleted_debeEnviarEvento() {
        
        Integer usuarioId = 123;
        Long notificacionId = 789L;
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(Long.class)
        );

        
        service.sendDeleted(usuarioId, notificacionId);

        
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                userCaptor.capture(),
                destinationCaptor.capture(),
                idCaptor.capture()
        );

        assertEquals("123", userCaptor.getValue(), "El ID de usuario debe ser '123'");
        assertEquals("/queue/notifications/deleted", destinationCaptor.getValue(),
                "El destino debe ser '/queue/notifications/deleted'");
        assertEquals(789L, idCaptor.getValue(), "El ID de notificación debe ser 789");
    }

    @Test
    @DisplayName("sendDeleted - debe manejar errores sin lanzar excepción")
    void sendDeleted_debeManejarlErroresSinLanzarExcepcion() {
        
        Integer usuarioId = 456;
        Long notificacionId = 111L;

        doThrow(new RuntimeException("Error enviando deleted"))
                .when(messagingTemplate).convertAndSendToUser(
                        anyString(),
                        anyString(),
                        any(Long.class)
                );

        
        assertDoesNotThrow(() -> service.sendDeleted(usuarioId, notificacionId),
                "No debe lanzar excepción cuando hay error enviando deleted");

        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("456"),
                eq("/queue/notifications/deleted"),
                eq(111L)
        );
    }

    @Test
    @DisplayName("Integración - debe enviar múltiples notificaciones a diferentes usuarios")
    void integracion_debeEnviarMultiplesNotificaciones() {
        
        NotificacionDTO notif1 = new NotificacionDTO();
        notif1.setId(1L);
        notif1.setNombre("Notificación 1");

        NotificacionDTO notif2 = new NotificacionDTO();
        notif2.setId(2L);
        notif2.setNombre("Notificación 2");

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any()
        );

        
        service.sendToUser(100, notif1);
        service.sendToUser(200, notif2);
        service.sendUnreadCount(100, 1L);
        service.sendUnreadCount(200, 2L);

        
        verify(messagingTemplate, times(4)).convertAndSendToUser(
                anyString(),
                anyString(),
                any()
        );

        verify(messagingTemplate).convertAndSendToUser("100", "/queue/notifications", notif1);
        verify(messagingTemplate).convertAndSendToUser("200", "/queue/notifications", notif2);
        verify(messagingTemplate).convertAndSendToUser("100", "/queue/notifications/count", 1L);
        verify(messagingTemplate).convertAndSendToUser("200", "/queue/notifications/count", 2L);
    }

    @Test
    @DisplayName("Integración - debe enviar broadcast y notificaciones individuales")
    void integracion_debeCombinarBroadcastYNotificacionesIndividuales() {
        
        doNothing().when(messagingTemplate).convertAndSend(
                anyString(),
                any(NotificacionDTO.class)
        );
        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(),
                anyString(),
                any(NotificacionDTO.class)
        );

        
        service.broadcast(notificacion);
        service.broadcastToRole(Rol.ADMIN, notificacion);
        service.sendToUser(123, notificacion);

        
        verify(messagingTemplate, times(2)).convertAndSend(
                anyString(),
                eq(notificacion)
        );
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                anyString(),
                anyString(),
                eq(notificacion)
        );
    }
}
