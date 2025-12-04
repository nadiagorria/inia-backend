package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;


@Data
public class ResetPasswordRequestDTO {
    private String email;              
    private String recoveryCode;       
    private String totpCode;           
    private String newPassword;        
}
