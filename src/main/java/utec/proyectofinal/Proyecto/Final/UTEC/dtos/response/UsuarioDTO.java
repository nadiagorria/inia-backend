package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UsuarioDTO {
    private Integer usuarioID;
    private String nombre;
    private String nombres;
    private String apellidos;
    private String email;
    
    
    private Rol rol;
    
    
    private List<String> roles;
    
    private EstadoUsuario estado;
    
    
    private String estadoSolicitud;
    
    private Boolean activo;
    
    
    private LocalDateTime fechaCreacion;
    
    
    private String fechaRegistro;
    
    private LocalDateTime fechaUltimaConexion;
    private String nombreCompleto;
}
