package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class TablaGermRequestDTO {
    
    
    private LocalDate fechaFinal;

    
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