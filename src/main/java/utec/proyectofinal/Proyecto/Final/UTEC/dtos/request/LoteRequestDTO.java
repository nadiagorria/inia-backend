package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class LoteRequestDTO {
    private String ficha;
    private String nomLote;
    private Long cultivarID;
    private String tipo;
    private Long empresaID;
    private Long clienteID;
    private String codigoCC;
    private String codigoFF;
    private LocalDate fechaEntrega;
    private LocalDate fechaRecibo;
    private Long depositoID;
    private String unidadEmbolsado;
    private String remitente;
    private String observaciones;
    private BigDecimal kilosLimpios;
    
    
    private List<DatosHumedadRequestDTO> datosHumedad;
    
    
    private Long numeroArticuloID;
    
    private Long origenID;
    private Long estadoID;
    private LocalDate fechaCosecha;
    
    
    private List<TipoAnalisis> tiposAnalisisAsignados;
}