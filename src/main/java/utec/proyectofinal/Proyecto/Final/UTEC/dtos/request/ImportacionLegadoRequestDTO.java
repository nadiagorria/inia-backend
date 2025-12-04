package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para la importación de datos legados desde Excel
 */
@Data
public class ImportacionLegadoRequestDTO {
    private MultipartFile archivo;
    private Boolean validarSoloSinImportar; 
}
