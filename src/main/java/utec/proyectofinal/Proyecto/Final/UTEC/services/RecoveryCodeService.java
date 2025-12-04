package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;


@Service
public class RecoveryCodeService {

    private static final int CODE_LENGTH = 8;
    private static final int EXPIRY_MINUTES = 10;
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; 
    private final SecureRandom random = new SecureRandom();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12); 

    
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

    
    public String hashCode(String plainCode) {
        if (plainCode == null || plainCode.isEmpty()) {
            throw new IllegalArgumentException("El código no puede estar vacío");
        }
        
        // Normalizar: uppercase y remover guiones
        String normalizedCode = plainCode.toUpperCase().replace("-", "");
        
        return passwordEncoder.encode(normalizedCode);
    }

    
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

    
    public LocalDateTime getExpiryTime() {
        return LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
    }

    
    public boolean isExpired(LocalDateTime expiryTime) {
        if (expiryTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }

    
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

    
    public int getExpiryMinutes() {
        return EXPIRY_MINUTES;
    }
}
