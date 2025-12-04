package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion;

@Data
public class NotificacionRequestDTO {
    private String nombre;
    private String mensaje;
    private Long usuarioId;
    private Long analisisId; 
    private TipoNotificacion tipo;
}