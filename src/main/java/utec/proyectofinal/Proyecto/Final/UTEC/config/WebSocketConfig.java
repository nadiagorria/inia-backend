package utec.proyectofinal.Proyecto.Final.UTEC.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configuración de WebSocket para notificaciones en tiempo real
 * 
 * ¿Qué hace esta clase?
 * - Habilita el uso de WebSocket con protocolo STOMP (Simple Text Oriented Messaging Protocol)
 * - STOMP es un protocolo simple sobre WebSocket que facilita el envío de mensajes
 * - Configura los endpoints y canales de comunicación
 * 
 * Componentes:
 * 1. Message Broker: Sistema que enruta mensajes a los clientes suscritos
 * 2. Endpoint: Punto de entrada para las conexiones WebSocket
 * 3. Channel Interceptor: Intercepta mensajes para autenticación/logging
 */
@Configuration
@EnableWebSocketMessageBroker  // Habilita WebSocket con broker de mensajes
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor authInterceptor;

    /**
     * Configura el broker de mensajes
     * 
     * ¿Qué es un broker?
     * Es como un "cartero" que distribuye mensajes a los destinatarios correctos
     * 
     * Prefijos configurados:
     * - /topic: Para broadcast (muchos usuarios reciben el mismo mensaje)
     * - /queue: Para mensajes privados (un usuario específico)
     * - /app: Prefijo para mensajes que vienen del cliente al servidor
     * - /user: Prefijo especial para enviar mensajes a usuarios específicos
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker simple en memoria
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefijo para mensajes desde el cliente
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefijo para mensajes dirigidos a usuarios específicos
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registra los endpoints STOMP
     * 
     * ¿Qué es un endpoint?
     * Es la URL donde los clientes se conectan al WebSocket
     * 
     * SockJS: Librería que proporciona fallback cuando WebSocket no está disponible
     * (navegadores antiguos, proxies corporativos, etc.)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")  
                .setAllowedOriginPatterns("*")     // CORS
                .withSockJS();                      // Habilitar fallback SockJS
    }

    /**
     * Configura interceptores de canales
     * 
     * ¿Para qué sirve?
     * Permite interceptar todos los mensajes para:
     * - Autenticar usuarios (validar JWT)
     * - Logging
     * - Validaciones de seguridad
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
