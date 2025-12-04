package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/**
 * DTO para listado paginado de legados
 */
@Data
public class LegadoListadoDTO {
    private Long legadoID;
    private String ficha;
    private String especie; 
    private LocalDate fechaRecibo;
    private Integer germC;
    private Integer germSC;
    private BigDecimal peso1000;
    private BigDecimal pura; 
    private BigDecimal puraI; 
}
