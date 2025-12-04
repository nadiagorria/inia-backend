package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AnalisisRequestDTO {
    private Long idLote;
    
    private String comentarios;
}