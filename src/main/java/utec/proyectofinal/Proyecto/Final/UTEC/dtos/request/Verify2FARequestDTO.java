package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

/**
 * DTO para verificar código 2FA durante el login
 */
@Data
public class Verify2FARequestDTO {
    private String totpCode;           
    private String deviceFingerprint;  
    private Boolean trustDevice;       
}
