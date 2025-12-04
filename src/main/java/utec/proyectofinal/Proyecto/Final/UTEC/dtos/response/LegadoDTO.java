package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LegadoDTO {
    private Long legadoID;
    
    
    private LoteDTO lote;
    
    
    private String codDoc;
    private String nomDoc;
    private String nroDoc;
    private LocalDate fechaDoc;
    private String familia;
    
    
    private String tipoSemilla;
    private String tipoTratGerm;

    
    
    private Integer germC;
    private Integer germSC;
    private BigDecimal peso1000;
    
    
    private BigDecimal pura;
    private BigDecimal oc;
    private BigDecimal porcOC;
    private BigDecimal maleza;
    private BigDecimal malezaTol;
    private BigDecimal matInerte;
    
    
    private BigDecimal puraI;
    private BigDecimal ocI;
    private BigDecimal malezaI;
    private BigDecimal malezaTolI;
    private BigDecimal matInerteI;
    
    
    private BigDecimal pesoHEC;
    private String nroTrans;
    private String ctaMov;
    private BigDecimal stk;
    
    
    private LocalDate fechaSC_I;
    private LocalDate fechaC_I;
    private Integer germTotalSC_I;
    private Integer germTotalC_I;
    
    
    private String otrasSemillasObser;
    private String semillaPura;
    private String semillaOtrosCultivos;
    private String semillaMalezas;
    private String semillaMalezasToleradas;
    private String materiaInerte;
    
    
    private LocalDate fechaImportacion;
    private String archivoOrigen;
    private Integer filaExcel;
    private Boolean activo;
}
