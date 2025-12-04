package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

import java.math.BigDecimal;

@Data
public class ValoresGermDTO {
    private Long valoresGermID;
    private Instituto instituto;
    private BigDecimal normales;
    private BigDecimal anormales;
    private BigDecimal duras;
    private BigDecimal frescas;
    private BigDecimal muertas;
    private BigDecimal germinacion;
    private Long tablaGermId; 
}
