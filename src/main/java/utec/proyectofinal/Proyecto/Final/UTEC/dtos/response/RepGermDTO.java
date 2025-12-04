package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.util.List;

import lombok.Data;

@Data
public class RepGermDTO {
    private Long repGermID;
    private Integer numRep;
    private List<Integer> normales;
    private Integer anormales;
    private Integer duras;
    private Integer frescas;
    private Integer muertas;
    private Integer total;
    private Long tablaGermId; 
}
