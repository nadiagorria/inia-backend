package utec.proyectofinal.Proyecto.Final.UTEC.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class SetupTokenService {
    
    private static final int TOKEN_EXPIRY_MINUTES = 5;
    private static final long TOKEN_EXPIRY_MS = TOKEN_EXPIRY_MINUTES * 60 * 1000;
    
    
    private final String JWT_SECRET = "@Z9@vQ3!pL8#wX7^tR2&nG6*yM4$eB1(dF0)sH5%kJ3&uY8*rE4#wQ1@zX6^nM9$";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    
    // Blacklist de tokens usados (previene reutilización)
    
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    
    
    private SecretKey getSigningKey() {
        return secretKey;
    }
    
    
    public String createSetupToken(Integer userId, String nombre, String qrCodeDataUrl, String totpSecret) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + TOKEN_EXPIRY_MS);
        
        // ID único del token (para blacklist y tracking)
        String tokenId = UUID.randomUUID().toString();
        
        // Construir claims del JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("nombre", nombre);
        claims.put("qrCodeDataUrl", qrCodeDataUrl);
        claims.put("totpSecret", totpSecret);
        claims.put("type", "admin_setup"); // Tipo de token para validación adicional
        
        // Generar token JWT firmado
        String token = Jwts.builder()
                .claims(claims)
                .id(tokenId) 
                .issuedAt(now) 
                .expiration(expiration) 
                .signWith(getSigningKey()) 
                .compact();
        
        System.out.println(" [SetupToken] Token JWT creado para usuario: " + nombre);
        System.out.println(" [JWT] Token ID: " + tokenId + " (expira en " + TOKEN_EXPIRY_MINUTES + " min)");
        
        // Limpiar blacklist de tokens expirados periódicamente
        cleanExpiredTokensFromBlacklist();
        
        return token;
    }
    
    
    public Map<String, Object> consumeSetupToken(String token) {
        try {
            // Parsear y validar token JWT
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey()) // Verifica firma HMAC
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Validar que sea un token de setup
            String type = claims.get("type", String.class);
            if (!"admin_setup".equals(type)) {
                System.err.println(" [SetupToken] Token inválido: tipo incorrecto");
                return null;
            }
            
            // Obtener ID del token
            String tokenId = claims.getId();
            
            // Verificar si ya fue usado (blacklist)
            if (tokenBlacklist.containsKey(tokenId)) {
                System.err.println(" [SetupToken] Token ya fue usado anteriormente (JTI: " + tokenId + ")");
                return null;
            }
            
            // Agregar a blacklist (un solo uso)
            // Guardamos el timestamp de expiración para limpieza posterior
            long expirationTime = claims.getExpiration().getTime();
            tokenBlacklist.put(tokenId, expirationTime);
            
            System.out.println(" [SetupToken] Token JWT validado y consumido exitosamente");
            System.out.println(" [JWT] Token agregado a blacklist (un solo uso)");
            
            // Extraer datos del token
            Map<String, Object> result = new HashMap<>();
            result.put("userId", claims.get("userId", Integer.class));
            result.put("nombre", claims.get("nombre", String.class));
            result.put("qrCodeDataUrl", claims.get("qrCodeDataUrl", String.class));
            result.put("totpSecret", claims.get("totpSecret", String.class));
            
            return result;
            
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.err.println(" [SetupToken] Token expirado: " + e.getMessage());
            return null;
            
        } catch (JwtException e) {
            System.err.println(" [SetupToken] Token JWT inválido: " + e.getMessage());
            return null;
            
        } catch (Exception e) {
            System.err.println(" [SetupToken] Error al procesar token: " + e.getMessage());
            return null;
        }
    }
    
    
    public void invalidateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String tokenId = claims.getId();
            long expirationTime = claims.getExpiration().getTime();
            tokenBlacklist.put(tokenId, expirationTime);
            
            System.out.println(" [SetupToken] Token invalidado manualmente (JTI: " + tokenId + ")");
            
        } catch (Exception e) {
            System.err.println(" [SetupToken] No se pudo invalidar token: " + e.getMessage());
        }
    }
    
    
    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Verificar tipo
            String type = claims.get("type", String.class);
            if (!"admin_setup".equals(type)) {
                return false;
            }
            
            // Verificar blacklist
            String tokenId = claims.getId();
            return !tokenBlacklist.containsKey(tokenId);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    
    private void cleanExpiredTokensFromBlacklist() {
        long now = System.currentTimeMillis();
        tokenBlacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
