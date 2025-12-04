package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoLote;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Lote")
@Data
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loteID;

    @Column(unique = true)
    private String ficha;


    @Column(unique = true)
    private String nomLote;

    @ManyToOne
    @JoinColumn(name = "cultivarID")
    private Cultivar cultivar;

    private TipoLote tipo;
    
    @ManyToOne
    @JoinColumn(name = "empresaID")
    private Contacto empresa;

    @ManyToOne
    @JoinColumn(name = "clienteID")
    private Contacto cliente;

    private String codigoCC;
    private String codigoFF;
    private LocalDate fechaEntrega;
    private LocalDate fechaRecibo;

    @ManyToOne
    @JoinColumn(name = "depositoID")
    private Catalogo deposito;

    private String unidadEmbolsado;
    private String remitente;
    private String observaciones;

    private BigDecimal kilosLimpios;
    
    
    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatosHumedad> datosHumedad;
    
    
    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Legado> datosLegados;
    
    @ManyToOne
    @JoinColumn(name = "numeroArticuloID")
    private Catalogo numeroArticulo;

    @ManyToOne
    @JoinColumn(name = "origenID")
    private Catalogo origen;
    
    @ManyToOne
    @JoinColumn(name = "estadoID")
    private Catalogo estado;
    private LocalDate fechaCosecha;
    
    
    @ElementCollection(targetClass = TipoAnalisis.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "lote_tipos_analisis", 
        joinColumns = @JoinColumn(name = "lote_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"lote_id", "tipo_analisis"})
    )
    @Column(name = "tipo_analisis")
    private List<TipoAnalisis> tiposAnalisisAsignados;
    
    private Boolean activo;
    
    
    public void setTiposAnalisisAsignados(List<TipoAnalisis> tipos) {
        if (tipos == null) {
            this.tiposAnalisisAsignados = null;
            return;
        }
        
        // Usar LinkedHashSet para mantener el orden y eliminar duplicados
        Set<TipoAnalisis> tiposUnicos = new LinkedHashSet<>(tipos);
        this.tiposAnalisisAsignados = new ArrayList<>(tiposUnicos);
    }
    
    public List<TipoAnalisis> getTiposAnalisisAsignados() {
        if (this.tiposAnalisisAsignados == null) {
            return new ArrayList<>();
        }
        
        // Asegurar que no hay duplicados al retornar
        Set<TipoAnalisis> tiposUnicos = new LinkedHashSet<>(this.tiposAnalisisAsignados);
        return new ArrayList<>(tiposUnicos);
    }
    
    public void agregarTipoAnalisis(TipoAnalisis tipo) {
        if (tipo == null) return;
        
        if (this.tiposAnalisisAsignados == null) {
            this.tiposAnalisisAsignados = new ArrayList<>();
        }
        
        if (!this.tiposAnalisisAsignados.contains(tipo)) {
            this.tiposAnalisisAsignados.add(tipo);
        }
    }
    
    public void removerTipoAnalisis(TipoAnalisis tipo) {
        if (this.tiposAnalisisAsignados != null) {
            this.tiposAnalisisAsignados.remove(tipo);
        }
    }
}

