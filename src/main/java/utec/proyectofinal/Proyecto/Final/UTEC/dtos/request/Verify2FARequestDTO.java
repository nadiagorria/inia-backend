package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;


@Data
public class Verify2FARequestDTO {
    private String totpCode;           
    private String deviceFingerprint;  
    private Boolean trustDevice;       
}
