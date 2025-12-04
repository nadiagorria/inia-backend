package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.BackupCode;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.BackupCodeRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class BackupCodeService {

    @Autowired
    private BackupCodeRepository backupCodeRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; 
    private static final int CODE_LENGTH = 12; 
    private static final int BACKUP_CODES_COUNT = 10;

    
    @Transactional
    public List<String> generateBackupCodes(Integer usuarioId) {
        // Eliminar códigos anteriores (si existen)
        backupCodeRepository.deleteAllByUsuarioId(usuarioId);
        
        List<String> plainCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < BACKUP_CODES_COUNT; i++) {
            // Generar código aleatorio
            String code = generateRandomCode(random);
            plainCodes.add(code);
            
            // Hashear y guardar
            BackupCode backupCode = new BackupCode();
            backupCode.setUsuarioId(usuarioId);
            backupCode.setCodeHash(passwordEncoder.encode(code.replace("-", ""))); 
            backupCodeRepository.save(backupCode);
        }
        
        System.out.println(" [BACKUP-CODES] Generados " + BACKUP_CODES_COUNT + " códigos para usuario ID: " + usuarioId);
        
        return plainCodes;
    }

    
    @Transactional
    public boolean verifyAndUseBackupCode(Integer usuarioId, String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        
        
        String normalizedCode = code.replace("-", "").replace(" ", "").toUpperCase();
        
        if (normalizedCode.length() != CODE_LENGTH) {
            System.err.println(" [BACKUP-CODE] Código con longitud incorrecta: " + normalizedCode.length());
            return false;
        }
        
        // Obtener todos los códigos no usados del usuario
        List<BackupCode> availableCodes = backupCodeRepository.findByUsuarioIdAndUsedFalse(usuarioId);
        
        System.out.println(" [BACKUP-CODE] Verificando código para usuario ID: " + usuarioId);
        System.out.println(" [BACKUP-CODE] Códigos disponibles: " + availableCodes.size());
        
        
        for (BackupCode backupCode : availableCodes) {
            if (passwordEncoder.matches(normalizedCode, backupCode.getCodeHash())) {
                
                backupCode.markAsUsed();
                backupCodeRepository.save(backupCode);
                
                long remaining = backupCodeRepository.countByUsuarioIdAndUsedFalse(usuarioId);
                System.out.println(" [BACKUP-CODE] Código válido usado. Códigos restantes: " + remaining);
                
                if (remaining <= 2) {
                    System.out.println(" [BACKUP-CODE] ADVERTENCIA: Solo quedan " + remaining + " códigos de respaldo");
                }
                
                return true;
            }
        }
        
        System.err.println(" [BACKUP-CODE] Código inválido o ya usado");
        return false;
    }

    
    public long getAvailableCodesCount(Integer usuarioId) {
        return backupCodeRepository.countByUsuarioIdAndUsedFalse(usuarioId);
    }

    
    public boolean hasAvailableCodes(Integer usuarioId) {
        return getAvailableCodesCount(usuarioId) > 0;
    }

    
    @Transactional
    public List<String> regenerateBackupCodes(Integer usuarioId) {
        System.out.println(" [BACKUP-CODE] Regenerando códigos para usuario ID: " + usuarioId);
        return generateBackupCodes(usuarioId);
    }

    
    @Transactional
    public void deleteAllUserCodes(Integer usuarioId) {
        backupCodeRepository.deleteAllByUsuarioId(usuarioId);
        System.out.println(" [BACKUP-CODE] Eliminados todos los códigos de usuario ID: " + usuarioId);
    }

    
    private String generateRandomCode(SecureRandom random) {
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            if (i > 0 && i % 4 == 0) {
                code.append("-");
            }
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return code.toString();
    }
}
