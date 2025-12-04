package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PmsRequestDTO extends AnalisisRequestDTO {
    
    private Integer numRepeticionesEsperadas;
    private Boolean esSemillaBrozosa;
}

