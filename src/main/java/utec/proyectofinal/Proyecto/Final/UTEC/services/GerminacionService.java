package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.specifications.GerminacionSpecification;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionEditRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoGerminacion;

@Service
public class GerminacionService {

    @Autowired
    private GerminacionRepository germinacionRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

    
    @Transactional
    public GerminacionDTO crearGerminacion(GerminacionRequestDTO solicitud) {
        try {
            System.out.println("Iniciando creación de germinación con solicitud: " + solicitud);
            
        Germinacion germinacion = mapearSolicitudAEntidad(solicitud);
        germinacion.setEstado(Estado.REGISTRADO);
        
        
        analisisService.establecerFechaInicio(germinacion);
        
        Germinacion germinacionGuardada = germinacionRepository.save(germinacion);            
            analisisHistorialService.registrarCreacion(germinacionGuardada);
            
            System.out.println("Germinación creada exitosamente con ID: " + germinacionGuardada.getAnalisisID());
            
            return mapearEntidadADTO(germinacionGuardada);
        } catch (Exception e) {
            System.err.println("Error al crear germinación: " + e.getMessage());
            throw new RuntimeException("Error al crear el análisis de germinación: " + e.getMessage());
        }
    }

    
    @Transactional
    public GerminacionDTO actualizarGerminacion(Long id, GerminacionRequestDTO solicitud) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            
            
            if (germinacion.getEstado() == Estado.APROBADO && analisisService.esAnalista()) {
                germinacion.setEstado(Estado.PENDIENTE_APROBACION);
                System.out.println("Análisis aprobado editado por analista - cambiando estado a PENDIENTE_APROBACION");
            }
            
            actualizarEntidadDesdeSolicitud(germinacion, solicitud);
            Germinacion germinacionActualizada = germinacionRepository.save(germinacion);
            
            
            analisisHistorialService.registrarModificacion(germinacionActualizada);
            
            return mapearEntidadADTO(germinacionActualizada);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    
    @Transactional
    public GerminacionDTO actualizarGerminacionSeguro(Long id, GerminacionEditRequestDTO dto) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            
            
            analisisService.manejarEdicionAnalisisFinalizado(germinacion);
            
            
            if (dto.getIdLote() != null) {
                Optional<Lote> loteOpt = loteRepository.findById(dto.getIdLote());
                if (loteOpt.isPresent()) {
                    germinacion.setLote(loteOpt.get());
                } else {
                    throw new RuntimeException("Lote no encontrado con ID: " + dto.getIdLote());
                }
            }
            if (dto.getComentarios() != null) {
                germinacion.setComentarios(dto.getComentarios());
            }
            
            
            Germinacion germinacionActualizada = germinacionRepository.save(germinacion);
            
            
            analisisHistorialService.registrarModificacion(germinacionActualizada);
            
            return mapearEntidadADTO(germinacionActualizada);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    
    public void eliminarGerminacion(Long id) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            germinacion.setActivo(false);
            germinacionRepository.save(germinacion);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    
    public void desactivarGerminacion(Long id) {
        analisisService.desactivarAnalisis(id, germinacionRepository);
    }

    
    public GerminacionDTO reactivarGerminacion(Long id) {
        return analisisService.reactivarAnalisis(id, germinacionRepository, this::mapearEntidadADTO);
    }

    
    public ResponseListadoGerminacion obtenerTodasGerminaciones() {
        List<Germinacion> germinacionesActivas = germinacionRepository.findByActivoTrue();
        List<GerminacionDTO> germinacionesDTO = germinacionesActivas.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
        
        ResponseListadoGerminacion response = new ResponseListadoGerminacion();
        response.setGerminaciones(germinacionesDTO);
        return response;
    }

    
    public Page<GerminacionListadoDTO> obtenerGerminacionesPaginadas(Pageable pageable) {
        Page<Germinacion> germinacionesPage = germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
        return germinacionesPage.map(this::mapearEntidadAListadoDTO);
    }

    
    public Page<GerminacionListadoDTO> obtenerGerminacionesPaginadasConFiltro(Pageable pageable, String filtroActivo) {
        Page<Germinacion> germinacionesPage;
        
        switch (filtroActivo.toLowerCase()) {
            case "activos":
                germinacionesPage = germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
                break;
            case "inactivos":
                germinacionesPage = germinacionRepository.findByActivoFalseOrderByFechaInicioDesc(pageable);
                break;
            default: 
                germinacionesPage = germinacionRepository.findAllByOrderByFechaInicioDesc(pageable);
                break;
        }
        
        return germinacionesPage.map(this::mapearEntidadAListadoDTO);
    }

    
    @Transactional(readOnly = true)
    public Page<GerminacionListadoDTO> obtenerGerminacionesPaginadasConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            String estado,
            Long loteId) {
        Specification<Germinacion> spec = GerminacionSpecification.conFiltros(searchTerm, activo, estado, loteId);
        Page<Germinacion> germinacionesPage = germinacionRepository.findAll(spec, pageable);
        return germinacionesPage.map(this::mapearEntidadAListadoDTO);
    }

    
    public GerminacionDTO obtenerGerminacionPorId(Long id) {
        Optional<Germinacion> germinacion = germinacionRepository.findById(id);
        if (germinacion.isPresent()) {
            return mapearEntidadADTO(germinacion.get());
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    
    public List<GerminacionDTO> obtenerGerminacionesPorIdLote(Long idLote) {
        List<Germinacion> germinaciones = germinacionRepository.findByIdLote(idLote);
        return germinaciones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    private boolean todasTablasFinalizadas(Germinacion germinacion) {
        if (germinacion.getTablaGerm() == null || germinacion.getTablaGerm().isEmpty()) {
            return false; 
        }
        
        for (TablaGerm tabla : germinacion.getTablaGerm()) {
            if (tabla.getFinalizada() == null || !tabla.getFinalizada()) {
                return false; 
            }
        }
        
        return true; 
    }

    /**
     * Validación completa para operaciones críticas de Germinación (finalizar y marcar para repetir)
     * Verifica completitud de tablas
     */
    private void validarGerminacionParaOperacionCritica(Germinacion germinacion) {
        
        if (!todasTablasFinalizadas(germinacion)) {
            throw new RuntimeException("No se puede completar la operación. Hay tablas pendientes de completar.");
        }
    }

    
    private Germinacion mapearSolicitudAEntidad(GerminacionRequestDTO solicitud) {
        System.out.println("Mapeando solicitud a entidad germinación");
        
        Germinacion germinacion = new Germinacion();
        
        
        germinacion.setComentarios(solicitud.getComentarios());
        
        
        if (solicitud.getIdLote() != null) {
            System.out.println("Buscando lote con ID: " + solicitud.getIdLote());
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                Lote lote = loteOpt.get();
                
                
                if (!lote.getActivo()) {
                    throw new RuntimeException("No se puede crear un análisis para un lote inactivo");
                }
                
                germinacion.setLote(lote);
                System.out.println("Lote encontrado y asignado: " + lote.getLoteID());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }
        
        System.out.println("Germinación mapeada exitosamente");
        return germinacion;
    }

    
    private void actualizarEntidadDesdeSolicitud(Germinacion germinacion, GerminacionRequestDTO solicitud) {
        System.out.println("Actualizando germinación desde solicitud");
        
        
        germinacion.setComentarios(solicitud.getComentarios());
        
        
        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                germinacion.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        System.out.println("Germinación actualizada exitosamente");
    }

    
    private GerminacionDTO mapearEntidadADTO(Germinacion germinacion) {
        GerminacionDTO dto = new GerminacionDTO();
        
        
        dto.setAnalisisID(germinacion.getAnalisisID());
        dto.setEstado(germinacion.getEstado());
        dto.setFechaInicio(germinacion.getFechaInicio());
        dto.setFechaFin(germinacion.getFechaFin());
        dto.setComentarios(germinacion.getComentarios());
        
        
        if (germinacion.getLote() != null) {
            dto.setIdLote(germinacion.getLote().getLoteID());
            dto.setLote(germinacion.getLote().getNomLote());
            dto.setFicha(germinacion.getLote().getFicha());
            
            
            if (germinacion.getLote().getCultivar() != null) {
                dto.setCultivarNombre(germinacion.getLote().getCultivar().getNombre());
                
                if (germinacion.getLote().getCultivar().getEspecie() != null) {
                    dto.setEspecieNombre(germinacion.getLote().getCultivar().getEspecie().getNombreComun());
                }
            }
        }
        
        
        dto.setHistorial(analisisHistorialService.obtenerHistorialAnalisis(germinacion.getAnalisisID()));
        
        return dto;
    }

    
    private GerminacionListadoDTO mapearEntidadAListadoDTO(Germinacion germinacion) {
        GerminacionListadoDTO dto = new GerminacionListadoDTO();
        
        
        dto.setAnalisisID(germinacion.getAnalisisID());
        dto.setEstado(germinacion.getEstado());
        dto.setFechaInicio(germinacion.getFechaInicio());
        dto.setFechaFin(germinacion.getFechaFin());
        dto.setActivo(germinacion.getActivo());
        
        
        if (germinacion.getLote() != null) {
            dto.setIdLote(germinacion.getLote().getLoteID());
            dto.setLote(germinacion.getLote().getNomLote());
            
            
            if (germinacion.getLote().getCultivar() != null && germinacion.getLote().getCultivar().getEspecie() != null) {
                String nombreEspecie = germinacion.getLote().getCultivar().getEspecie().getNombreComun();
                
                if (nombreEspecie == null || nombreEspecie.trim().isEmpty()) {
                    nombreEspecie = germinacion.getLote().getCultivar().getEspecie().getNombreCientifico();
                }
                dto.setEspecie(nombreEspecie);
            }
        }
        
        
        dto.setCumpleNorma(germinacion.getEstado() != Estado.A_REPETIR);
        
        
        if (germinacion.getTablaGerm() != null && !germinacion.getTablaGerm().isEmpty()) {
            TablaGerm tablaGerm = germinacion.getTablaGerm().get(0);
            
            
            dto.setFechaInicioGerm(tablaGerm.getFechaGerminacion());
            dto.setFechaFinal(tablaGerm.getFechaFinal());
            
            
            dto.setTienePrefrio(tablaGerm.getTienePrefrio());
            dto.setTienePretratamiento(tablaGerm.getTienePretratamiento());
            
            
            if (tablaGerm.getValoresGerm() != null && !tablaGerm.getValoresGerm().isEmpty()) {
                for (var valorGerm : tablaGerm.getValoresGerm()) {
                    if (valorGerm.getInstituto() != null) {
                        if (valorGerm.getInstituto().toString().equals("INIA")) {
                            dto.setValorGerminacionINIA(valorGerm.getGerminacion());
                        } else if (valorGerm.getInstituto().toString().equals("INASE")) {
                            dto.setValorGerminacionINASE(valorGerm.getGerminacion());
                        }
                    }
                }
            }
        }
        
        
        if (germinacion.getAnalisisID() != null) {
            var historial = analisisHistorialService.obtenerHistorialAnalisis(germinacion.getAnalisisID());
            if (!historial.isEmpty()) {
                
                var primerRegistro = historial.get(historial.size() - 1);
                dto.setUsuarioCreador(primerRegistro.getUsuario());
                
                
                var ultimoRegistro = historial.get(0);
                dto.setUsuarioModificador(ultimoRegistro.getUsuario());
            }
        }
        
        return dto;
    }

    /**
     * Finalizar análisis según el rol del usuario
     * - Analistas: pasa a PENDIENTE_APROBACION
     * - Administradores: pasa directamente a APROBADO
     */
    public GerminacionDTO finalizarAnalisis(Long id) {
        return analisisService.finalizarAnalisisGenerico(
            id,
            germinacionRepository,
            this::mapearEntidadADTO,
            this::validarGerminacionParaOperacionCritica
        );
    }

    /**
     * Aprobar análisis (solo administradores)
     */
    public GerminacionDTO aprobarAnalisis(Long id) {
        return analisisService.aprobarAnalisisGenerico(
            id,
            germinacionRepository,
            this::mapearEntidadADTO,
            this::validarGerminacionParaOperacionCritica, 
            germinacionRepository::findByIdLote 
        );
    }

    
    public GerminacionDTO marcarParaRepetir(Long id) {
        return analisisService.marcarParaRepetirGenerico(
            id,
            germinacionRepository,
            this::mapearEntidadADTO,
            this::validarGerminacionParaOperacionCritica 
        );
    }
}