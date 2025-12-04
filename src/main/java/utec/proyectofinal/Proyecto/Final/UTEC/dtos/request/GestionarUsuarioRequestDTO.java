package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

@Data
public class GestionarUsuarioRequestDTO {
    private Rol rol;                    
    private EstadoUsuario estado;       
    private Boolean activo;             
}