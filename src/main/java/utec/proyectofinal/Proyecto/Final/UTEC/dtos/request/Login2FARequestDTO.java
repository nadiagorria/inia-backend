package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

/**
 * DTO extendido para login con soporte 2FA
 */
@Data
public class Login2FARequestDTO {
    private String usuario;              
    private String password;             
    private String totpCode;             
    private String deviceFingerprint;    
    private Boolean trustDevice;         
}
