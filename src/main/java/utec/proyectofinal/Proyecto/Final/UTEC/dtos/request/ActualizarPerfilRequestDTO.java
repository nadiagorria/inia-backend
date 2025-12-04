package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class ActualizarPerfilRequestDTO {
    private String nombre;         
    private String nombres;        
    private String apellidos;      
    private String email;          
    private String contraseniaActual;  
    private String contraseniaNueva;   
}