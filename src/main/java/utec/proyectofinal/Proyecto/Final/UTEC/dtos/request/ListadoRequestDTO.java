package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;

@Data
public class ListadoRequestDTO {
    private TipoListado listadoTipo;
    private Instituto listadoInsti;
    private Integer listadoNum;
    private Long idCatalogo;  
    private Long idEspecie;   
}
