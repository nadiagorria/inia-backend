package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

@Data
public class AprobarUsuarioRequestDTO {
    private Rol rol;               
}