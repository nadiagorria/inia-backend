package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Servicio para generar y validar códigos de recuperación de contraseña
 * 
 * SEGURIDAD:
 * - Genera códigos aleatorios de 8 caracteres alfanuméricos
 * - Los códigos se hashean con BCrypt antes de almacenarlos
 * - Los códigos expiran en 10 minutos
 * - Un código solo se puede usar una vez
 * - Requiere 2FA adicional para resetear la contraseña
 */
@Service
public class RecoveryCodeService {

    private static final int CODE_LENGTH = 8;
    private static final int EXPIRY_MINUTES = 10;
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; 
    private final SecureRandom random = new SecureRandom();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12); 

    /**
     * Genera un código de recuperación aleatorio de 8 caracteres
     * 
     * Formato: XXXX-XXXX (con guion para facilitar lectura)
     * Ejemplo: AB3K-7M9P
     * 
     * @return Código en texto plano (NUNCA almacenar este valor directamente)
     */
    public String generateRecoveryCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
            
            // Agregar guion en la mitad para facilitar lectura
            if (i == 3) {
                code.append("-");
            }
        }
        
        return code.toString();
    }

    /**
     * Hashea un código de recuperación para almacenamiento seguro
     * 
     * @param plainCode Código en texto plano
     * @return Hash BCrypt del código
     */
    public String hashCode(String plainCode) {
        if (plainCode == null || plainCode.isEmpty()) {
            throw new IllegalArgumentException("El código no puede estar vacío");
        }
        
        // Normalizar: uppercase y remover guiones
        String normalizedCode = plainCode.toUpperCase().replace("-", "");
        
        return passwordEncoder.encode(normalizedCode);
    }

    /**
     * Verifica que un código ingresado coincida con el hash almacenado
     * 
     * @param plainCode Código ingresado por el usuario
     * @param hashedCode Hash almacenado en la base de datos
     * @return true si el código es correcto
     */
    public boolean verifyCode(String plainCode, String hashedCode) {
        if (plainCode == null || hashedCode == null) {
            return false;
        }
        
        try {
            // Normalizar el código ingresado
            String normalizedCode = plainCode.toUpperCase().replace("-", "").trim();
            
            
            if (normalizedCode.length() != CODE_LENGTH) {
                return false;
            }
            
            return passwordEncoder.matches(normalizedCode, hashedCode);
        } catch (Exception e) {
            System.err.println(" Error verificando código de recuperación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Calcula la fecha de expiración del código (10 minutos desde ahora)
     * 
     * @return Timestamp de expiración
     */
    public LocalDateTime getExpiryTime() {
        return LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
    }

    /**
     * Verifica si un código ha expirado
     * 
     * @param expiryTime Fecha de expiración del código
     * @return true si el código ha expirado
     */
    public boolean isExpired(LocalDateTime expiryTime) {
        if (expiryTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Valida el formato de un código de recuperación
     * 
     * @param code Código a validar
     * @return true si el formato es válido
     */
    public boolean isValidFormat(String code) {
        if (code == null) {
            return false;
        }
        
        String normalized = code.toUpperCase().replace("-", "").trim();
        
        
        if (normalized.length() != CODE_LENGTH) {
            return false;
        }
        
        // Solo caracteres válidos
        for (char c : normalized.toCharArray()) {
            if (CHARACTERS.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Obtiene el tiempo de validez del código en minutos
     * 
     * @return Minutos de validez (10)
     */
    public int getExpiryMinutes() {
        return EXPIRY_MINUTES;
    }
}
