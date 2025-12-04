package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Setup2FAResponseDTO {
    private String secret;          
    private String qrCodeDataUrl;   
    private String issuer;          
    private String accountName;     
}
