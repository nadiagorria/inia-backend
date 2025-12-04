package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad para almacenar datos históricos importados desde Excel
 * que no se ajustan completamente al modelo actual de análisis
 */
@Entity
@Table(name = "Legado")
@Data
public class Legado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long legadoID;

    
    @ManyToOne
    @JoinColumn(name = "loteID")
    private Lote lote;

    // Datos del documento
    private String codDoc;
    private String nomDoc;
    private String nroDoc;
    private LocalDate fechaDoc;
    private String familia;

    // Información del tipo de semilla y tratamiento
    private String tipoSemilla;
    private String tipoTratGerm;

    // Datos de germinación
    private Integer germC;
    private Integer germSC;
    private BigDecimal peso1000;

    // Datos de pureza
    private BigDecimal pura;
    private BigDecimal oc;
    private BigDecimal porcOC;
    private BigDecimal maleza;
    private BigDecimal malezaTol;
    private BigDecimal matInerte;

    // Datos de pureza I (inicial?)
    private BigDecimal puraI;
    private BigDecimal ocI;
    private BigDecimal malezaI;
    private BigDecimal malezaTolI;
    private BigDecimal matInerteI;

    // Peso y otros datos
    private BigDecimal pesoHEC;

    // Datos de transacción
    private String nroTrans;
    private String ctaMov;

    // Stock
    private BigDecimal stk;

    // Fechas adicionales
    private LocalDate fechaSC_I;
    private LocalDate fechaC_I;

    // Germinación total
    private Integer germTotalSC_I;
    private Integer germTotalC_I;

    // Observaciones y detalles
    @Column(length = 1000)
    private String otrasSemillasObser;

    // Análisis detallado de semillas
    @Column(length = 500)
    private String semillaPura;

    @Column(length = 500)
    private String semillaOtrosCultivos;

    @Column(length = 500)
    private String semillaMalezas;

    @Column(length = 500)
    private String semillaMalezasToleradas;

    @Column(length = 500)
    private String materiaInerte;

    // Metadatos de importación
    private LocalDate fechaImportacion;
    private String archivoOrigen;
    private Integer filaExcel;

    private Boolean activo = true;
}
