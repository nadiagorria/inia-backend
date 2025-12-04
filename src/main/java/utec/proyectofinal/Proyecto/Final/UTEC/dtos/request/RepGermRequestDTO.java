package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import java.util.List;

import lombok.Data;

@Data
public class RepGermRequestDTO {
    private List<Integer> normales;
    private Integer anormales;
    private Integer duras;
    private Integer frescas;
    private Integer muertas;
    
    
}