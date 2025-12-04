package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AnalisisHistorialDTO {
    private Long id;
    private String usuario;
    private LocalDateTime fechaHora;
    private String accion; 
}
