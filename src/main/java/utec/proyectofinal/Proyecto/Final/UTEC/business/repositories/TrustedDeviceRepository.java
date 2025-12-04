package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TrustedDevice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

    
    Optional<TrustedDevice> findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(
        Integer usuarioId, 
        String deviceFingerprintHash
    );

    
    List<TrustedDevice> findByUsuarioIdAndActiveTrueOrderByLastUsedAtDesc(Integer usuarioId);

    
    long countByUsuarioIdAndActiveTrue(Integer usuarioId);

    
    @Modifying
    @Query("UPDATE TrustedDevice t SET t.active = false WHERE t.expiresAt < :now AND t.active = true")
    int deactivateExpiredDevices(LocalDateTime now);

    
    @Modifying
    @Query("UPDATE TrustedDevice t SET t.active = false WHERE t.usuarioId = :usuarioId AND t.active = true")
    int deactivateAllUserDevices(Integer usuarioId);
}
