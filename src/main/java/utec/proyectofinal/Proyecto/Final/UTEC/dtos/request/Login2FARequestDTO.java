package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;


@Data
public class Login2FARequestDTO {
    private String usuario;              
    private String password;             
    private String totpCode;             
    private String deviceFingerprint;    
    private Boolean trustDevice;         
}
