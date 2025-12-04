package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;

@Entity
@Table(name = "Contacto")
@Data
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contactoID;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String contacto; 
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContacto tipo;
    
    @Column(nullable = false)
    private Boolean activo = true;
}