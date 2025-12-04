package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurezaRequestDTO extends AnalisisRequestDTO {
    
    private LocalDate fecha;
    private Boolean cumpleEstandar;
    private BigDecimal pesoInicial_g;
    private BigDecimal semillaPura_g;
    private BigDecimal materiaInerte_g;
    private BigDecimal otrosCultivos_g;
    private BigDecimal malezas_g;
    private BigDecimal malezasToleradas_g;
    private BigDecimal malezasTolCero_g;
    private BigDecimal pesoTotal_g;

    private BigDecimal redonSemillaPura;
    private BigDecimal redonMateriaInerte;
    private BigDecimal redonOtrosCultivos;
    private BigDecimal redonMalezas;
    private BigDecimal redonMalezasToleradas;
    private BigDecimal redonMalezasTolCero;
    private BigDecimal redonPesoTotal;

    private BigDecimal inasePura;
    private BigDecimal inaseMateriaInerte;
    private BigDecimal inaseOtrosCultivos;
    private BigDecimal inaseMalezas;
    private BigDecimal inaseMalezasToleradas;
    private BigDecimal inaseMalezasTolCero;
    private LocalDate inaseFecha;

    private List<ListadoRequestDTO> otrasSemillas;
}
