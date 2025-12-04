package utec.proyectofinal.Proyecto.Final.UTEC.services;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Servicio para gestionar códigos TOTP (Time-based One-Time Password)
 * Compatible con Google Authenticator, Microsoft Authenticator, Authy, etc.
 * 
 * SEGURIDAD:
 * - Usa algoritmo HMAC-SHA1 (estándar RFC 6238)
 * - Códigos de 6 dígitos
 * - Ventana de tiempo de 30 segundos
 * - Permite discrepancia de 1 período (30s antes/después) para tolerancia de reloj
 */
@Service
public class TotpService {

    private static final String ISSUER = "INIA"; 
    private final SecretGenerator secretGenerator;
    private final TimeProvider timeProvider;
    private final CodeGenerator codeGenerator;
    private final CodeVerifier codeVerifier;

    public TotpService() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.timeProvider = new SystemTimeProvider();
        this.codeGenerator = new DefaultCodeGenerator();
        
        // Configurar verificador con discrepancia de 1 período (tolera hasta 30s de diferencia)
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        verifier.setTimePeriod(30);        // Período de 30 segundos
        verifier.setAllowedTimePeriodDiscrepancy(1); // Tolera 1 período de diferencia (30s antes/después)
        this.codeVerifier = verifier;
    }

    /**
     * Genera un nuevo secret key para TOTP
     * 
     * @return Secret en formato Base32 (compatible con Google Authenticator)
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * Genera un QR code para configurar Google Authenticator
     * 
     * @param secret El secret key del usuario
     * @param accountName Nombre de la cuenta (generalmente el email o username)
     * @return QR code en formato Base64 Data URL (para mostrar en <img src="">)
     */
    public String generateQrCodeDataUrl(String secret, String accountName) {
        try {
            System.out.println(" [TOTP-QR] Generando QR code...");
            System.out.println(" [TOTP-QR] Secret usado para QR: " + secret);
            System.out.println(" [TOTP-QR] Account name: " + accountName);
            
            QrData data = new QrData.Builder()
                .label(accountName)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1) // Google Authenticator usa SHA1
                .digits(6)                         // Códigos de 6 dígitos
                .period(30)                        // 30 segundos por código
                .build();

            QrGenerator generator = new ZxingPngQrGenerator();
            byte[] imageData = generator.generate(data);
            String mimeType = generator.getImageMimeType();
            
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            String dataUrl = String.format("data:%s;base64,%s", mimeType, base64Image);
            
            System.out.println(" [TOTP-QR] QR code generado exitosamente");
            
            return dataUrl;
            
        } catch (QrGenerationException e) {
            throw new RuntimeException("Error generando QR code para 2FA: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica un código TOTP ingresado por el usuario
     * 
     * @param secret El secret key del usuario
     * @param code El código de 6 dígitos ingresado
     * @return true si el código es válido, false en caso contrario
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }
        
        // Limpiar el código (remover espacios, guiones, etc.)
        String cleanCode = code.replaceAll("[^0-9]", "");
        
        if (cleanCode.length() != 6) {
            return false;
        }
        
        try {
            boolean isValid = codeVerifier.isValidCode(secret, cleanCode);
            
            // DEBUG: Generar código actual para comparar
            if (!isValid) {
                try {
                    long currentTimeMillis = System.currentTimeMillis();
                    long currentBucket = Math.floorDiv(currentTimeMillis, 30000);
                    String currentCode = codeGenerator.generate(secret, currentBucket);
                    
                    System.err.println(" [TOTP] ===== DEBUG TOTP =====");
                    System.err.println(" [TOTP] Timestamp actual (ms): " + currentTimeMillis);
                    System.err.println(" [TOTP] Time bucket calculado: " + currentBucket);
                    System.err.println(" [TOTP] Código esperado (actual): " + currentCode);
                    System.err.println(" [TOTP] Código recibido: " + cleanCode);
                    
                    // Probar con buckets anteriores y posteriores (ventana de tolerancia)
                    System.err.println(" [TOTP] Probando ventana de tiempo:");
                    for (int i = -3; i <= 3; i++) {
                        long testBucket = currentBucket + i;
                        String testCode = codeGenerator.generate(secret, testBucket);
                        String status = testCode.equals(cleanCode) ? " MATCH!" : "  ";
                        System.err.println(" [TOTP]   Bucket " + testBucket + " (offset " + i + "): " + testCode + " " + status);
                    }
                    
                    System.err.println(" [TOTP] ========================");
                } catch (Exception e) {
                    System.err.println(" [TOTP] No se pudo generar código de debug: " + e.getMessage());
                }
            }
            
            return isValid;
        } catch (Exception e) {
            System.err.println(" Error verificando código TOTP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera el código actual (útil para testing o debugging)
     * NO usar en producción para mostrar al usuario
     * 
     * @param secret El secret key
     * @return El código de 6 dígitos actual
     */
    public String getCurrentCode(String secret) {
        try {
            long currentBucket = Math.floorDiv(timeProvider.getTime(), 30);
            return codeGenerator.generate(secret, currentBucket);
        } catch (Exception e) {
            throw new RuntimeException("Error generando código TOTP: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el tiempo restante (en segundos) hasta que el código actual expire
     * 
     * @return Segundos restantes (0-30)
     */
    public int getRemainingSeconds() {
        long currentTime = timeProvider.getTime();
        return (int) (30 - (currentTime % 30));
    }

    /**
     * Verifica que un secret sea válido (formato Base32 correcto)
     * 
     * @param secret El secret a validar
     * @return true si es válido
     */
    public boolean isValidSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            return false;
        }
        
        // Debe tener entre 16-32 caracteres
        if (secret.length() < 16 || secret.length() > 32) {
            return false;
        }
        
        // Solo debe contener caracteres Base32 válidos (A-Z, 2-7)
        return secret.matches("^[A-Z2-7]+$");
    }
}
