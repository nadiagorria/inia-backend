package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PmsDTO extends AnalisisDTO{
    
    private Integer numRepeticionesEsperadas;
    private Integer numTandas;
    private Boolean esSemillaBrozosa;
    
    
    private BigDecimal promedio100g;
    private BigDecimal desvioStd;
    private BigDecimal coefVariacion;
    private BigDecimal pmssinRedon;
    private BigDecimal pmsconRedon;
}
