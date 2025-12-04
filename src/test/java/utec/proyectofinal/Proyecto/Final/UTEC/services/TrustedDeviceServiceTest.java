package utec.proyectofinal.Proyecto.Final.UTEC.services;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TrustedDevice;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TrustedDeviceRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TrustedDeviceDTO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TrustedDeviceService
 * 
 * Funcionalidades testeadas:
 * - Hashing de fingerprints con SHA-256
 * - Verificación de dispositivos de confianza
 * - Registro de nuevos dispositivos
 * - Límite de dispositivos por usuario (5 máximo)
 * - Revocación de dispositivos
 * - Limpieza de dispositivos expirados
 * - Actualización de último uso
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TrustedDeviceService (Dispositivos de Confianza)")
class TrustedDeviceServiceTest {

    @Mock
    private TrustedDeviceRepository trustedDeviceRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TrustedDeviceService trustedDeviceService;

    @BeforeEach
    void setUp() {
        
    }

    @Test
    @DisplayName("Hashear fingerprint - debe retornar hash SHA-256 de 64 caracteres")
    void hashFingerprint_debeRetornarHashSHA256() {
        
        String fingerprint = "test-fingerprint-123";

        
        String hash = trustedDeviceService.hashFingerprint(fingerprint);

        
        assertNotNull(hash, "El hash no debe ser nulo");
        assertEquals(64, hash.length(), "El hash SHA-256 debe tener 64 caracteres hexadecimales");
        assertTrue(hash.matches("[0-9a-f]{64}"), "El hash debe ser hexadecimal");
    }

    @Test
    @DisplayName("Hashear mismo fingerprint dos veces - debe generar mismo hash")
    void hashFingerprint_mismoFingerprint_debeDevolverMismoHash() {
        
        String fingerprint = "consistent-fingerprint";

        
        String hash1 = trustedDeviceService.hashFingerprint(fingerprint);
        String hash2 = trustedDeviceService.hashFingerprint(fingerprint);

        
        assertEquals(hash1, hash2, "El hash debe ser consistente para el mismo fingerprint");
    }

    @Test
    @DisplayName("Hashear fingerprints diferentes - debe generar hashes diferentes")
    void hashFingerprint_fingerprintsDiferentes_debeGenerarHashesDiferentes() {
        
        String fingerprint1 = "fingerprint-1";
        String fingerprint2 = "fingerprint-2";

        
        String hash1 = trustedDeviceService.hashFingerprint(fingerprint1);
        String hash2 = trustedDeviceService.hashFingerprint(fingerprint2);

        
        assertNotEquals(hash1, hash2, "Fingerprints diferentes deben generar hashes diferentes");
    }

    @Test
    @DisplayName("Verificar dispositivo de confianza - dispositivo existente y activo debe retornar true")
    void isTrustedDevice_dispositivoExistenteYActivo_debeRetornarTrue() {
        
        Integer usuarioId = 1;
        String fingerprint = "valid-fingerprint";
        String fingerprintHash = trustedDeviceService.hashFingerprint(fingerprint);

        TrustedDevice device = new TrustedDevice();
        device.setUsuarioId(usuarioId);
        device.setDeviceFingerprintHash(fingerprintHash);
        device.setActive(true);
        device.setExpiresAt(LocalDateTime.now().plusDays(30));
        device.setLastUsedAt(LocalDateTime.now());

        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(usuarioId, fingerprintHash))
            .thenReturn(Optional.of(device));
        when(trustedDeviceRepository.save(any(TrustedDevice.class))).thenReturn(device);

        
        boolean isTrusted = trustedDeviceService.isTrustedDevice(usuarioId, fingerprint);

        
        assertTrue(isTrusted, "El dispositivo existente y activo debe ser de confianza");
        verify(trustedDeviceRepository).save(device); 
    }

    @Test
    @DisplayName("Verificar dispositivo de confianza - dispositivo no existente debe retornar false")
    void isTrustedDevice_dispositivoNoExistente_debeRetornarFalse() {
        
        Integer usuarioId = 1;
        String fingerprint = "unknown-fingerprint";
        String fingerprintHash = trustedDeviceService.hashFingerprint(fingerprint);

        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(usuarioId, fingerprintHash))
            .thenReturn(Optional.empty());

        
        boolean isTrusted = trustedDeviceService.isTrustedDevice(usuarioId, fingerprint);

        
        assertFalse(isTrusted, "Un dispositivo no registrado no debe ser de confianza");
    }

    @Test
    @DisplayName("Verificar dispositivo de confianza - dispositivo expirado debe retornar false")
    void isTrustedDevice_dispositivoExpirado_debeRetornarFalse() {
        
        Integer usuarioId = 1;
        String fingerprint = "expired-fingerprint";
        String fingerprintHash = trustedDeviceService.hashFingerprint(fingerprint);

        TrustedDevice device = new TrustedDevice();
        device.setUsuarioId(usuarioId);
        device.setDeviceFingerprintHash(fingerprintHash);
        device.setActive(true);
        device.setExpiresAt(LocalDateTime.now().minusDays(1)); 

        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(usuarioId, fingerprintHash))
            .thenReturn(Optional.of(device));
        when(trustedDeviceRepository.save(any(TrustedDevice.class))).thenReturn(device);

        
        boolean isTrusted = trustedDeviceService.isTrustedDevice(usuarioId, fingerprint);

        
        assertFalse(isTrusted, "Un dispositivo expirado no debe ser de confianza");
        assertFalse(device.getActive(), "El dispositivo expirado debe ser desactivado");
        verify(trustedDeviceRepository).save(device);
    }

    @Test
    @DisplayName("Verificar dispositivo con fingerprint vacío - debe retornar false")
    void isTrustedDevice_fingerprintVacio_debeRetornarFalse() {
        
        boolean isTrusted = trustedDeviceService.isTrustedDevice(1, "");

        
        assertFalse(isTrusted, "Un fingerprint vacío debe retornar false");
        verify(trustedDeviceRepository, never()).findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(anyInt(), anyString());
    }

    @Test
    @DisplayName("Registrar nuevo dispositivo - debe crear dispositivo correctamente")
    void trustDevice_nuevoDispositivo_debeCrearCorrectamente() {
        
        Integer usuarioId = 1;
        String fingerprint = "new-device-fingerprint";
        String fingerprintHash = trustedDeviceService.hashFingerprint(fingerprint);

        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(usuarioId, fingerprintHash))
            .thenReturn(Optional.empty());
        when(trustedDeviceRepository.countByUsuarioIdAndActiveTrue(usuarioId)).thenReturn(2L);
        when(trustedDeviceRepository.save(any(TrustedDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        TrustedDevice device = trustedDeviceService.trustDevice(usuarioId, fingerprint, request);

        
        assertNotNull(device);
        assertEquals(usuarioId, device.getUsuarioId());
        assertEquals(fingerprintHash, device.getDeviceFingerprintHash());
        assertTrue(device.getActive());
        verify(trustedDeviceRepository).save(any(TrustedDevice.class));
    }

    @Test
    @DisplayName("Registrar dispositivo existente - debe actualizar lastUsedAt")
    void trustDevice_dispositivoExistente_debeActualizarLastUsedAt() {
        
        Integer usuarioId = 1;
        String fingerprint = "existing-fingerprint";
        String fingerprintHash = trustedDeviceService.hashFingerprint(fingerprint);

        TrustedDevice existingDevice = new TrustedDevice();
        existingDevice.setUsuarioId(usuarioId);
        existingDevice.setDeviceFingerprintHash(fingerprintHash);
        existingDevice.setActive(true);
        existingDevice.setLastUsedAt(LocalDateTime.now().minusDays(5));
        existingDevice.setExpiresAt(LocalDateTime.now().plusDays(55));

        
        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(usuarioId, fingerprintHash))
            .thenReturn(Optional.of(existingDevice));
        when(trustedDeviceRepository.save(any(TrustedDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        TrustedDevice device = trustedDeviceService.trustDevice(usuarioId, fingerprint, request);

        
        assertNotNull(device);
        verify(trustedDeviceRepository).save(existingDevice);
    }

    @Test
    @DisplayName("Registrar dispositivo - límite alcanzado debe lanzar excepción")
    void trustDevice_limiteAlcanzado_debeLanzarExcepcion() {
        
        Integer usuarioId = 1;
        String fingerprint = "new-fingerprint";
        String fingerprintHash = trustedDeviceService.hashFingerprint(fingerprint);

        
        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(usuarioId, fingerprintHash))
            .thenReturn(Optional.empty());
        when(trustedDeviceRepository.countByUsuarioIdAndActiveTrue(usuarioId)).thenReturn(5L); 

        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> trustedDeviceService.trustDevice(usuarioId, fingerprint, request));
        assertTrue(exception.getMessage().contains("Límite de dispositivos"));
        verify(trustedDeviceRepository, never()).save(any(TrustedDevice.class));
    }

    @Test
    @DisplayName("Registrar dispositivo con fingerprint vacío - debe lanzar excepción")
    void trustDevice_fingerprintVacio_debeLanzarExcepcion() {
        
        

        
        assertThrows(IllegalArgumentException.class, 
            () -> trustedDeviceService.trustDevice(1, "", request));
        verify(trustedDeviceRepository, never()).save(any(TrustedDevice.class));
    }

    @Test
    @DisplayName("Listar dispositivos del usuario - debe retornar lista de DTOs")
    void listUserDevices_debeRetornarListaDeDTOs() {
        
        Integer usuarioId = 1;
        
        TrustedDevice device1 = new TrustedDevice();
        device1.setId(1L);
        device1.setUsuarioId(usuarioId);
        device1.setDeviceName("Chrome en Windows");
        device1.setActive(true);
        device1.setCreatedAt(LocalDateTime.now().minusDays(10));
        device1.setLastUsedAt(LocalDateTime.now());
        device1.setExpiresAt(LocalDateTime.now().plusDays(50));

        TrustedDevice device2 = new TrustedDevice();
        device2.setId(2L);
        device2.setUsuarioId(usuarioId);
        device2.setDeviceName("Safari en iPhone");
        device2.setActive(true);
        device2.setCreatedAt(LocalDateTime.now().minusDays(5));
        device2.setLastUsedAt(LocalDateTime.now().minusDays(1));
        device2.setExpiresAt(LocalDateTime.now().plusDays(55));

        when(trustedDeviceRepository.findByUsuarioIdAndActiveTrueOrderByLastUsedAtDesc(usuarioId))
            .thenReturn(Arrays.asList(device1, device2));

        
        List<TrustedDeviceDTO> devices = trustedDeviceService.listUserDevices(usuarioId);

        
        assertNotNull(devices);
        assertEquals(2, devices.size());
        assertEquals("Chrome en Windows", devices.get(0).getDeviceName());
        assertEquals("Safari en iPhone", devices.get(1).getDeviceName());
    }

    @Test
    @DisplayName("Revocar dispositivo - usuario autorizado debe revocar correctamente")
    void revokeDevice_usuarioAutorizado_debeRevocarCorrectamente() {
        
        Long deviceId = 1L;
        Integer usuarioId = 1;

        TrustedDevice device = new TrustedDevice();
        device.setId(deviceId);
        device.setUsuarioId(usuarioId);
        device.setActive(true);

        when(trustedDeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(trustedDeviceRepository.save(any(TrustedDevice.class))).thenReturn(device);

        
        trustedDeviceService.revokeDevice(deviceId, usuarioId);

        
        assertFalse(device.getActive(), "El dispositivo debe ser desactivado");
        verify(trustedDeviceRepository).save(device);
    }

    @Test
    @DisplayName("Revocar dispositivo - usuario no autorizado debe lanzar excepción")
    void revokeDevice_usuarioNoAutorizado_debeLanzarExcepcion() {
        
        Long deviceId = 1L;
        Integer usuarioId = 1;
        Integer otroUsuarioId = 2;

        TrustedDevice device = new TrustedDevice();
        device.setId(deviceId);
        device.setUsuarioId(otroUsuarioId); 
        device.setActive(true);

        when(trustedDeviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> trustedDeviceService.revokeDevice(deviceId, usuarioId));
        assertTrue(exception.getMessage().contains("No autorizado"));
        verify(trustedDeviceRepository, never()).save(any(TrustedDevice.class));
    }

    @Test
    @DisplayName("Revocar dispositivo inexistente - debe lanzar excepción")
    void revokeDevice_dispositivoInexistente_debeLanzarExcepcion() {
        
        Long deviceId = 999L;
        when(trustedDeviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, 
            () -> trustedDeviceService.revokeDevice(deviceId, 1));
    }

    @Test
    @DisplayName("Revocar todos los dispositivos del usuario - debe desactivar todos")
    void revokeAllUserDevices_debeDesactivarTodos() {
        
        Integer usuarioId = 1;
        when(trustedDeviceRepository.deactivateAllUserDevices(usuarioId)).thenReturn(3);

        
        trustedDeviceService.revokeAllUserDevices(usuarioId);

        
        verify(trustedDeviceRepository).deactivateAllUserDevices(usuarioId);
    }

    @Test
    @DisplayName("Limpiar dispositivos expirados - debe desactivar dispositivos expirados")
    void cleanupExpiredDevices_debeDesactivarExpirados() {
        
        when(trustedDeviceRepository.deactivateExpiredDevices(any(LocalDateTime.class))).thenReturn(5);

        
        trustedDeviceService.cleanupExpiredDevices();

        
        verify(trustedDeviceRepository).deactivateExpiredDevices(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Extraer nombre de dispositivo - Chrome en Windows")
    void extractDeviceName_ChromeEnWindows() {
        
        when(request.getHeader("User-Agent"))
            .thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(anyInt(), anyString()))
            .thenReturn(Optional.empty());
        when(trustedDeviceRepository.countByUsuarioIdAndActiveTrue(anyInt())).thenReturn(0L);
        when(trustedDeviceRepository.save(any(TrustedDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        TrustedDevice device = trustedDeviceService.trustDevice(1, "fingerprint", request);

        
        assertTrue(device.getDeviceName().contains("Chrome"));
        assertTrue(device.getDeviceName().contains("Windows"));
    }

    @Test
    @DisplayName("Extraer nombre de dispositivo - Firefox en Linux")
    void extractDeviceName_FirefoxEnLinux() {
        
        when(request.getHeader("User-Agent"))
            .thenReturn("Mozilla/5.0 (X11; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(anyInt(), anyString()))
            .thenReturn(Optional.empty());
        when(trustedDeviceRepository.countByUsuarioIdAndActiveTrue(anyInt())).thenReturn(0L);
        when(trustedDeviceRepository.save(any(TrustedDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        TrustedDevice device = trustedDeviceService.trustDevice(1, "fingerprint", request);

        
        assertTrue(device.getDeviceName().contains("Firefox"));
        assertTrue(device.getDeviceName().contains("Linux"));
    }

    @Test
    @DisplayName("Extraer IP de request - debe obtener IP correctamente")
    void extractIpAddress_debeObtenerIPCorrectamente() {
        
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0");
        when(request.getRemoteAddr()).thenReturn("203.0.113.42");
        when(trustedDeviceRepository.findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(anyInt(), anyString()))
            .thenReturn(Optional.empty());
        when(trustedDeviceRepository.countByUsuarioIdAndActiveTrue(anyInt())).thenReturn(0L);
        when(trustedDeviceRepository.save(any(TrustedDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        TrustedDevice device = trustedDeviceService.trustDevice(1, "fingerprint", request);

        
        assertEquals("203.0.113.42", device.getIpAddress());
    }
}
