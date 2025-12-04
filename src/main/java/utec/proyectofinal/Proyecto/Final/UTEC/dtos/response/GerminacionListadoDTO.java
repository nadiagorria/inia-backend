package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
@Data
public class GerminacionListadoDTO {
    private Long analisisID;
    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String lote; 
    private Long idLote;
    private String especie; 
    private Boolean activo;
    
    
    
    
    private String usuarioCreador;
    private String usuarioModificador;
    private Boolean cumpleNorma; 
    
    
    private BigDecimal valorGerminacionINIA;
    private BigDecimal valorGerminacionINASE;
    private LocalDate fechaInicioGerm;
    private LocalDate fechaFinal;
    private Boolean tienePrefrio;
    private Boolean tienePretratamiento;
}