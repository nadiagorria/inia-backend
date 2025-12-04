package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    private Boolean leido = false;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "analisis_id")
    private Long analisisId;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

}