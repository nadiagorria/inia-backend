package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class ImportacionLegadoRequestDTO {
    private MultipartFile archivo;
    private Boolean validarSoloSinImportar; 
}
