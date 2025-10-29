package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class ValidacionLoteDTO {
    private String ficha;
    private String nomLote;
    private Long loteID; // Para excluir el lote actual en caso de edición
}