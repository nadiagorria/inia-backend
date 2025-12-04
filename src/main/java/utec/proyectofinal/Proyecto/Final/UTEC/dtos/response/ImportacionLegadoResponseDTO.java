package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportacionLegadoResponseDTO {
    private Integer totalFilas;
    private Integer filasImportadas;
    private Integer filasConErrores;
    private List<ErrorImportacionDTO> errores = new ArrayList<>();
    private String mensaje;
    private Boolean exitoso;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorImportacionDTO {
        private Integer fila;
        private String campo;
        private String error;
        private String valorOriginal;
    }
}
