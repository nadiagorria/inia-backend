package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class TablaGermDTO {
    private Long tablaGermID;

    private List<RepGermDTO> repGerm;

    private Integer total;
    private List<BigDecimal> promedioSinRedondeo;
    private List<BigDecimal> promediosSinRedPorConteo;

    
    private BigDecimal porcentajeNormalesConRedondeo;
    private BigDecimal porcentajeAnormalesConRedondeo;
    private BigDecimal porcentajeDurasConRedondeo;
    private BigDecimal porcentajeFrescasConRedondeo;
    private BigDecimal porcentajeMuertasConRedondeo;

    private List<ValoresGermDTO> valoresGerm;

    private LocalDate fechaFinal;

    
    private Boolean finalizada;

    
    private String tratamiento;
    private String productoYDosis;
    private Integer numSemillasPRep;
    private String metodo;
    private String temperatura;
    
    
    private Boolean tienePrefrio;
    private String descripcionPrefrio;
    
    
    private Boolean tienePretratamiento;
    private String descripcionPretratamiento;
    
    
    private LocalDate fechaIngreso; 
    private LocalDate fechaGerminacion; 
    private List<LocalDate> fechaConteos;
    private LocalDate fechaUltConteo;
    private String numDias;
    
    
    private Integer numeroRepeticiones;
    private Integer numeroConteos;
    
    
    private Integer diasPrefrio;
}