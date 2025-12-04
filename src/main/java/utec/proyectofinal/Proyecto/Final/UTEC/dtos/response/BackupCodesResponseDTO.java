package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;


@Data
@AllArgsConstructor
public class BackupCodesResponseDTO {
    private List<String> codes;
    private int totalCodes;
    private String mensaje;
}
