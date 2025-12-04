package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

/**
 * DTO para resetear contraseña con código de recuperación y 2FA
 */
@Data
public class ResetPasswordRequestDTO {
    private String email;              
    private String recoveryCode;       
    private String totpCode;           
    private String newPassword;        
}
