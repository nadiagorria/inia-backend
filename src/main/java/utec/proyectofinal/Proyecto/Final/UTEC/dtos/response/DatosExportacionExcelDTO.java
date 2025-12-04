
package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class DatosExportacionExcelDTO {
    
    
    private String especie;
    private String variedad;
    private String lote;
    private String deposito;
    private String numeroArticulo;
    private String numeroAnalisis;
    private String numeroFicha;  
    private String nombreLote;    
    private String kilos;
    
    
    private BigDecimal humedad;
    
    
    private BigDecimal purezaSemillaPura;
    private BigDecimal purezaMateriaInerte;
    private BigDecimal purezaOtrosCultivos;
    private BigDecimal purezaMalezas;
    private BigDecimal purezaMalezasToleradas;
    private BigDecimal purezaMalezasToleranciaC;
    
    
    private BigDecimal purezaInaseSemillaPura;
    private BigDecimal purezaInaseMateriaInerte;
    private BigDecimal purezaInaseOtrosCultivos;
    private BigDecimal purezaInaseMalezas;
    private BigDecimal purezaInaseMalezasToleradas;
    private BigDecimal purezaInaseMalezasToleranciaC;
    
    
    private String descripcionMalezas;
    private String descripcionOtrosCultivos;
    private String descripcionMalezasToleradas;
    private String descripcionMalezasToleranciaC;
    
    
    private String descripcionInaseMalezas;
    private String descripcionInaseOtrosCultivos;
    private String descripcionInaseMalezasToleradas;
    private String descripcionInaseMalezasToleranciaC;
    
    
    private String dosnOtrosCultivos;           
    private String dosnMalezas;                 
    private String dosnMalezasToleradas;        
    private String dosnMalezasToleranciaC;   
    private String dosnBrassica;                
    
    
    private String dosnInaseOtrosCultivos;      
    private String dosnInaseMalezas;            
    private String dosnInaseMalezasToleradas;   
    private String dosnInaseMalezasToleranciaC; 
    private String dosnInaseBrassica;           
    
    
    private BigDecimal pms;
    
    
    private LocalDate fechaAnalisis;
    private String tratamientoSemillas;
    
    
    private BigDecimal germinacionPlantulasNormales;
    private BigDecimal germinacionPlantulasAnormales;
    private BigDecimal germinacionSemillasDeterioras;
    private BigDecimal germinacionSemillasFrescas;
    private BigDecimal germinacionSemillasMuertas;
    private BigDecimal germinacionTotal;
    
    
    private BigDecimal germinacionInasePlantulasNormales;
    private BigDecimal germinacionInasePlantulasAnormales;
    private BigDecimal germinacionInaseSemillasDeterioras;
    private BigDecimal germinacionInaseSemillasFrescas;
    private BigDecimal germinacionInaseSemillasMuertas;
    private BigDecimal germinacionInaseTotal;
    
    
    private BigDecimal viabilidadPorcentaje;
    private BigDecimal viabilidadInasePorcentaje;
}