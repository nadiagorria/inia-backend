package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "trusted_devices", indexes = {
    @Index(name = "idx_user_device", columnList = "usuario_id,device_fingerprint_hash")
})
@Data
public class TrustedDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    
    @Column(name = "device_fingerprint_hash", nullable = false, length = 64)
    private String deviceFingerprintHash;

    
    @Column(name = "device_name", length = 100)
    private String deviceName;

    
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    
    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(60);
        }
    }

    
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(60);
    }

    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    
    public boolean isValid() {
        return active && !isExpired();
    }
}
