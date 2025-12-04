package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class LoteDTO {
    private Long loteID;
    private String ficha;
    private String nomLote;
    private Long cultivarID;
    private String cultivarNombre;
    private String especieNombre;
    private String tipo;
    private Long empresaID;
    private String empresaNombre;
    private Long clienteID;
    private String clienteNombre;
    private String codigoCC;
    private String codigoFF;
    private LocalDate fechaEntrega;
    private LocalDate fechaRecibo;
    private Long depositoID;
    private String depositoValor;
    private String unidadEmbolsado;
    private String remitente;
    private String observaciones;
    private BigDecimal kilosLimpios;
    
    
    private List<DatosHumedadDTO> datosHumedad;
    
    
    private Long numeroArticuloID;
    private String numeroArticuloValor;
    
    private Long origenID;
    private String origenValor;
    private Long estadoID;
    private String estadoValor;
    private LocalDate fechaCosecha;
    
    
    private List<TipoAnalisis> tiposAnalisisAsignados;
    
    private Boolean activo;
}