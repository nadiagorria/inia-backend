package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class ValidacionLoteResponseDTO {
    private boolean fichaExiste;
    private boolean nomLoteExiste;
    
    
    public ValidacionLoteResponseDTO() {
        this.fichaExiste = false;
        this.nomLoteExiste = false;
    }
}