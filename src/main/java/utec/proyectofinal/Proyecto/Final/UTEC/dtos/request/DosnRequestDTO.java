package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoDOSN;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DosnRequestDTO {
    private Long idLote;
    
    private Boolean cumpleEstandar;
    private String comentarios;

    private LocalDate fechaINIA;
    private BigDecimal gramosAnalizadosINIA;
    private List<TipoDOSN> tipoINIA;

    private LocalDate fechaINASE;
    private BigDecimal gramosAnalizadosINASE;
    private List<TipoDOSN> tipoINASE;

    private List<CuscutaRegistroRequestDTO> cuscutaRegistros;

    private List<ListadoRequestDTO> listados;
}
