package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.time.LocalDateTime;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Data
public class DosnListadoDTO {
    private Long analisisID;
    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String lote; 
    private Long idLote;
    private String especie; 
    private Boolean activo;
    
    
    private Boolean cumpleEstandar;
    
    private String usuarioCreador;
    private String usuarioModificador;
}
