package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public abstract class AnalisisDTO {

    private Long analisisID;

    private Long idLote; 
    private String lote; 
    private String ficha; 
    private String cultivarNombre; 
    private String especieNombre; 

    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private String comentarios;

    private Boolean activo;

    private List<AnalisisHistorialDTO> historial;
}
