package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExportacionRequestDTO {
    
    private List<Long> loteIds;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private List<Long> especieIds;
    private List<Long> cultivarIds;
    private Boolean incluirInactivos = false;
    private List<String> tiposAnalisis; 
    
    
    private Boolean incluirEncabezados = true;
    private Boolean incluirColoresEstilo = true;
    private String formatoFecha = "dd/MM/yyyy";
}