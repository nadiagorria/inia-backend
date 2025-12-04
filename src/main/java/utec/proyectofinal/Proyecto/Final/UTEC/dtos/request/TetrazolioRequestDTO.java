package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class TetrazolioRequestDTO extends AnalisisRequestDTO {
    
    private Integer numSemillasPorRep;
    private String pretratamiento;
    private String concentracion;
    private Integer tincionHs;
    private Integer tincionTemp;
    private LocalDate fecha;
    private Integer numRepeticionesEsperadas;
    private BigDecimal viabilidadInase;
}