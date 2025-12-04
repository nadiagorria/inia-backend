package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DatosHumedadRequestDTO {
    private Long tipoHumedadID; 
    private BigDecimal valor;
}