package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "backup_codes", indexes = {
    @Index(name = "idx_backup_codes_user_unused", columnList = "usuario_id,used")
})
@Data
public class BackupCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    
    @Column(name = "code_hash", nullable = false, length = 60)
    private String codeHash;

    
    @Column(name = "used", nullable = false)
    private Boolean used = false;

    
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (used == null) {
            used = false;
        }
    }

    
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    
    public boolean isAvailable() {
        return !used;
    }
}
