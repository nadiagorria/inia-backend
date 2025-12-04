package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO de respuesta al configurar 2FA
 * Contiene el QR code y el secret key para configurar Google Authenticator
 */
@Data
@AllArgsConstructor
public class Setup2FAResponseDTO {
    private String secret;          
    private String qrCodeDataUrl;   
    private String issuer;          
    private String accountName;     
}
