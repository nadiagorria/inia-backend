package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PMS")
@Data
public class Pms extends Analisis {

    // Campos de configuración inicial
    private Integer numRepeticionesEsperadas;
    private Integer numTandas;
    private Boolean esSemillaBrozosa;
    
    // Campos calculados
    private BigDecimal promedio100g;
    private BigDecimal desvioStd;
    private BigDecimal coefVariacion;
    private BigDecimal pmssinRedon;
    private BigDecimal pmsconRedon;

    @OneToMany(mappedBy = "pms", cascade = CascadeType.ALL)
    private List<RepPms> repPms;

    
    

}
