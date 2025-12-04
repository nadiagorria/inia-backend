package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "TablaGerm")
@Data
public class TablaGerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tablaGermID;

    @OneToMany(mappedBy = "tablaGerm", cascade = CascadeType.ALL)
    private List<RepGerm> repGerm;

    private Integer total;

    @ElementCollection
    private List<BigDecimal> promedioSinRedondeo;

    @ElementCollection
    private List<BigDecimal> promediosSinRedPorConteo;

    // Campos de porcentaje con redondeo (5 campos ingresados manualmente)
    private BigDecimal porcentajeNormalesConRedondeo;
    private BigDecimal porcentajeAnormalesConRedondeo;
    private BigDecimal porcentajeDurasConRedondeo;
    private BigDecimal porcentajeFrescasConRedondeo;
    private BigDecimal porcentajeMuertasConRedondeo;

    @OneToMany(mappedBy = "tablaGerm", cascade = CascadeType.ALL)
    private List<ValoresGerm> valoresGerm;

    private LocalDate fechaFinal;

    // Campo de control para finalización
    private Boolean finalizada = false;

    // Campos movidos desde Germinacion
    private String tratamiento;
    private String productoYDosis;
    private Integer numSemillasPRep;
    private String metodo;
    private String temperatura;
    
    // Campos de prefrío (boolean + descripción + días)
    private Boolean tienePrefrio;
    private String descripcionPrefrio;
    
    // Campos de pretratamiento (boolean + descripción, sin días)
    private Boolean tienePretratamiento;
    private String descripcionPretratamiento;

    // Campos de fechas y control de conteos movidos desde Germinacion
    private LocalDate fechaIngreso; 
    private LocalDate fechaGerminacion; 

    @ElementCollection
    private List<LocalDate> fechaConteos;

    private LocalDate fechaUltConteo;

    private String numDias;
    
    // Campos de control
    private Integer numeroRepeticiones;
    private Integer numeroConteos;
    
    // Campo para días de prefrío
    private Integer diasPrefrio;

    @ManyToOne
    @JoinColumn(name = "germinacionID")
    private Germinacion germinacion;
}