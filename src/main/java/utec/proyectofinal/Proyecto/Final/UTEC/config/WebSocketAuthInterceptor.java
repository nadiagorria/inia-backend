package utec.proyectofinal.Proyecto.Final.UTEC.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;

import java.util.List;


@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Obtener el accessor para manipular headers del mensaje STOMP
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);

        // Solo validar en el comando CONNECT (cuando el cliente se conecta)
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            
            // Extraer el header Authorization
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Extraer el token (remover "Bearer ")
                String token = authHeader.substring(7);
                
                try {
                    // Validar el token
                    if (jwtUtil.esTokenValido(token)) {
                        // Extraer información del usuario
                        String username = jwtUtil.obtenerUsuarioDelToken(token);
                        Integer userId = jwtUtil.obtenerUserIdDelToken(token);
                        
                        // Crear una autenticación de Spring Security
                        
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userId.toString(),  
                                null,               
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                        
                        // Asociar el usuario autenticado a esta sesión WebSocket
                        
                        accessor.setUser(authentication);
                        
                        System.out.println(" WebSocket autenticado: Usuario " + username + " (ID: " + userId + ")");
                    } else {
                        System.err.println(" Token JWT inválido en WebSocket");
                        return null; 
                    }
                } catch (Exception e) {
                    System.err.println(" Error validando token en WebSocket: " + e.getMessage());
                    return null; 
                }
            } else {
                System.err.println(" WebSocket sin token de autenticación");
                return null; 
            }
        }

        return message; 
    }

    
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                System.out.println(" Usuario " + accessor.getUser().getName() + " desconectado del WebSocket");
            }
        }
    }
}
