package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String usuario;    
    private String password;   
}