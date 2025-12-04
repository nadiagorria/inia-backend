package utec.proyectofinal.Proyecto.Final.UTEC.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TrustedDevice;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TrustedDeviceRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TrustedDeviceDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class TrustedDeviceService {

    @Autowired
    private TrustedDeviceRepository trustedDeviceRepository;

    private static final int MAX_DEVICES_PER_USER = 5; // Máximo 5 dispositivos de confianza

    
    public String hashFingerprint(String fingerprint) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprint.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando hash SHA-256: " + e.getMessage(), e);
        }
    }

    
    public boolean isTrustedDevice(Integer usuarioId, String fingerprint) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            return false;
        }
        
        String fingerprintHash = hashFingerprint(fingerprint);
        Optional<TrustedDevice> deviceOpt = trustedDeviceRepository
            .findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(usuarioId, fingerprintHash);
        
        if (deviceOpt.isEmpty()) {
            return false;
        }
        
        TrustedDevice device = deviceOpt.get();
        
        // Verificar si no está expirado
        if (device.isExpired()) {
            // Desactivar el dispositivo expirado
            device.setActive(false);
            trustedDeviceRepository.save(device);
            return false;
        }
        
        // Actualizar última vez usado
        device.updateLastUsed();
        trustedDeviceRepository.save(device);
        
        return true;
    }

    
    @Transactional
    public TrustedDevice trustDevice(Integer usuarioId, String fingerprint, HttpServletRequest request) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            throw new IllegalArgumentException("Fingerprint del dispositivo es requerido");
        }
        
        String fingerprintHash = hashFingerprint(fingerprint);
        
        // Verificar si ya existe
        Optional<TrustedDevice> existingOpt = trustedDeviceRepository
            .findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(usuarioId, fingerprintHash);
        
        if (existingOpt.isPresent()) {
            // Ya existe, actualizar última vez usado
            TrustedDevice existing = existingOpt.get();
            existing.updateLastUsed();
            return trustedDeviceRepository.save(existing);
        }
        
        // Verificar límite de dispositivos
        long deviceCount = trustedDeviceRepository.countByUsuarioIdAndActiveTrue(usuarioId);
        if (deviceCount >= MAX_DEVICES_PER_USER) {
            throw new RuntimeException("Límite de dispositivos de confianza alcanzado (" + MAX_DEVICES_PER_USER + ")");
        }
        
        // Crear nuevo dispositivo
        TrustedDevice device = new TrustedDevice();
        device.setUsuarioId(usuarioId);
        device.setDeviceFingerprintHash(fingerprintHash);
        device.setDeviceName(extractDeviceName(request));
        device.setUserAgent(request.getHeader("User-Agent"));
        device.setIpAddress(extractIpAddress(request));
        device.setActive(true);
        
        return trustedDeviceRepository.save(device);
    }

    
    public List<TrustedDeviceDTO> listUserDevices(Integer usuarioId) {
        return trustedDeviceRepository.findByUsuarioIdAndActiveTrueOrderByLastUsedAtDesc(usuarioId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    
    @Transactional
    public void revokeDevice(Long deviceId, Integer usuarioId) {
        TrustedDevice device = trustedDeviceRepository.findById(deviceId)
            .orElseThrow(() -> new RuntimeException("Dispositivo no encontrado"));
        
        if (!device.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado para revocar este dispositivo");
        }
        
        device.setActive(false);
        trustedDeviceRepository.save(device);
    }

    
    @Transactional
    public void revokeAllUserDevices(Integer usuarioId) {
        trustedDeviceRepository.deactivateAllUserDevices(usuarioId);
    }

    
    @Transactional
    public void cleanupExpiredDevices() {
        int deactivated = trustedDeviceRepository.deactivateExpiredDevices(LocalDateTime.now());
        if (deactivated > 0) {
            System.out.println(" Limpieza: " + deactivated + " dispositivos expirados desactivados");
        }
    }

    
    private String extractDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Dispositivo Desconocido";
        }
        
        String browser = "Navegador";
        String os = "SO Desconocido";
        
        // Detectar navegador
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            browser = "Safari";
        } else if (userAgent.contains("Edg")) {
            browser = "Edge";
        }
        
        // Detectar sistema operativo
        if (userAgent.contains("Windows")) {
            os = "Windows";
        } else if (userAgent.contains("Mac")) {
            os = "macOS";
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
        } else if (userAgent.contains("Android")) {
            os = "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
        }
        
        return browser + " en " + os;
    }

    
    private String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs (proxies), tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    
    private TrustedDeviceDTO convertToDTO(TrustedDevice device) {
        return new TrustedDeviceDTO(
            device.getId(),
            device.getDeviceName(),
            device.getUserAgent(),
            device.getIpAddress(),
            device.getCreatedAt(),
            device.getLastUsedAt(),
            device.getExpiresAt(),
            device.getActive()
        );
    }
}
