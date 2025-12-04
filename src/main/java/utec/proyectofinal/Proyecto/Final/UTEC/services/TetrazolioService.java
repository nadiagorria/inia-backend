package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepTetrazolioViabilidadRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.specifications.TetrazolioSpecification;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeadosRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TetrazolioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import java.math.BigDecimal;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoTetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioListadoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class TetrazolioService {

    @Autowired
    private TetrazolioRepository tetrazolioRepository;

    @Autowired
    private RepTetrazolioViabilidadRepository repeticionRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

    
    @Transactional
    public TetrazolioDTO crearTetrazolio(TetrazolioRequestDTO solicitud) {
        try {
        
          
           
            Tetrazolio tetrazolio = mapearSolicitudAEntidad(solicitud);
            tetrazolio.setEstado(Estado.REGISTRADO);
            
            
            analisisService.establecerFechaInicio(tetrazolio);
            
            Tetrazolio tetrazolioGuardado = tetrazolioRepository.save(tetrazolio);
            
            
            analisisHistorialService.registrarCreacion(tetrazolioGuardado);
            
            return mapearEntidadADTO(tetrazolioGuardado);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el análisis de tetrazolio: " + e.getMessage());
        }
    }

    
    @Transactional
    public TetrazolioDTO actualizarTetrazolio(Long id, TetrazolioRequestDTO solicitud) {
        Tetrazolio tetrazolio = tetrazolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id));
        
        
        Estado estadoOriginal = tetrazolio.getEstado();
        
        if (estadoOriginal == Estado.APROBADO && analisisService.esAnalista()) {
            
            tetrazolio.setEstado(Estado.PENDIENTE_APROBACION);
        }
        
        
        
        
        actualizarEntidadDesdeSolicitud(tetrazolio, solicitud);
        
        Tetrazolio tetrazolioActualizado = tetrazolioRepository.save(tetrazolio);
        
        
        analisisHistorialService.registrarModificacion(tetrazolioActualizado);
        
        return mapearEntidadADTO(tetrazolioActualizado);
    }


    
    public void desactivarTetrazolio(Long id) {
        analisisService.desactivarAnalisis(id, tetrazolioRepository);
    }

    
    public TetrazolioDTO reactivarTetrazolio(Long id) {
        return analisisService.reactivarAnalisis(id, tetrazolioRepository, this::mapearEntidadADTO);
    }

    
    public ResponseListadoTetrazolio obtenerTodosTetrazolio() {
        List<Tetrazolio> tetrazoliosActivos = tetrazolioRepository.findByActivoTrue();
        List<TetrazolioDTO> tetrazoliosDTO = tetrazoliosActivos.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
        
        ResponseListadoTetrazolio response = new ResponseListadoTetrazolio();
        response.setTetrazolios(tetrazoliosDTO);
        return response;
    }

    
    public TetrazolioDTO obtenerTetrazolioPorId(Long id) {
        Tetrazolio tetrazolio = tetrazolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id));
        
        return mapearEntidadADTO(tetrazolio);
    }

    
    public List<TetrazolioDTO> obtenerTetrazoliosPorIdLote(Long idLote) {
        List<Tetrazolio> tetrazolios = tetrazolioRepository.findByIdLote(idLote);
        return tetrazolios.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public Page<TetrazolioListadoDTO> obtenerTetrazoliosPaginadas(Pageable pageable) {
        Page<Tetrazolio> tetrazolioPage = tetrazolioRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
        return tetrazolioPage.map(this::mapearEntidadAListadoDTO);
    }

    
    public Page<TetrazolioListadoDTO> obtenerTetrazoliosPaginadasConFiltro(Pageable pageable, String filtroActivo) {
        Page<Tetrazolio> tetrazolioPage;
        
        switch (filtroActivo.toLowerCase()) {
            case "activos":
                tetrazolioPage = tetrazolioRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
                break;
            case "inactivos":
                tetrazolioPage = tetrazolioRepository.findByActivoFalseOrderByFechaInicioDesc(pageable);
                break;
            default: 
                tetrazolioPage = tetrazolioRepository.findAllByOrderByFechaInicioDesc(pageable);
                break;
        }
        
        return tetrazolioPage.map(this::mapearEntidadAListadoDTO);
    }

    
    public Page<TetrazolioListadoDTO> obtenerTetrazoliosPaginadasConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            String estado,
            Long loteId) {
        
        Specification<Tetrazolio> spec = TetrazolioSpecification.conFiltros(searchTerm, activo, estado, loteId);
        Page<Tetrazolio> tetrazolioPage = tetrazolioRepository.findAll(spec, pageable);
        return tetrazolioPage.map(this::mapearEntidadAListadoDTO);
    }

    
    private TetrazolioListadoDTO mapearEntidadAListadoDTO(Tetrazolio tetrazolio) {
        TetrazolioListadoDTO dto = new TetrazolioListadoDTO();
        dto.setAnalisisID(tetrazolio.getAnalisisID());
        dto.setEstado(tetrazolio.getEstado());
        dto.setFechaInicio(tetrazolio.getFechaInicio());
        dto.setFechaFin(tetrazolio.getFechaFin());
        dto.setActivo(tetrazolio.getActivo());
        dto.setFecha(tetrazolio.getFecha());
        
        
        dto.setViabilidadConRedondeo(tetrazolio.getPorcViablesRedondeo());
        
        
        dto.setViabilidadInase(tetrazolio.getViabilidadInase());
        
        if (tetrazolio.getLote() != null) {
            dto.setIdLote(tetrazolio.getLote().getLoteID());
            dto.setLote(tetrazolio.getLote().getNomLote()); 
            
            
            if (tetrazolio.getLote().getCultivar() != null && tetrazolio.getLote().getCultivar().getEspecie() != null) {
                String nombreEspecie = tetrazolio.getLote().getCultivar().getEspecie().getNombreComun();
                
                if (nombreEspecie == null || nombreEspecie.trim().isEmpty()) {
                    nombreEspecie = tetrazolio.getLote().getCultivar().getEspecie().getNombreCientifico();
                }
                dto.setEspecie(nombreEspecie);
            }
        }
        
        if (tetrazolio.getAnalisisID() != null) {
            var historial = analisisHistorialService.obtenerHistorialAnalisis(tetrazolio.getAnalisisID());
            if (!historial.isEmpty()) {
                var primerRegistro = historial.get(historial.size() - 1);
                dto.setUsuarioCreador(primerRegistro.getUsuario());
                var ultimoRegistro = historial.get(0);
                dto.setUsuarioModificador(ultimoRegistro.getUsuario());
            }
        }
        return dto;
    }

    
    @Transactional
    public TetrazolioDTO actualizarPorcentajesRedondeados(Long id, PorcentajesRedondeadosRequestDTO solicitud) {
        Tetrazolio tetrazolio = tetrazolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id));
        
        
        validarCompletitudRepeticiones(tetrazolio);
        
        
        tetrazolio.setPorcViablesRedondeo(solicitud.getPorcViablesRedondeo());
        tetrazolio.setPorcNoViablesRedondeo(solicitud.getPorcNoViablesRedondeo());
        tetrazolio.setPorcDurasRedondeo(solicitud.getPorcDurasRedondeo());
        
        Tetrazolio tetrazolioActualizado = tetrazolioRepository.save(tetrazolio);
        System.out.println("Porcentajes redondeados actualizados exitosamente para tetrazolio ID: " + id);
        return mapearEntidadADTO(tetrazolioActualizado);
    }

    
    private Tetrazolio mapearSolicitudAEntidad(TetrazolioRequestDTO solicitud) {
        System.out.println("Mapeando solicitud a entidad tetrazolio");
        
        
        if (solicitud.getNumRepeticionesEsperadas() == null || solicitud.getNumRepeticionesEsperadas() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de repeticiones esperadas (mayor a 0).");
        }
        
        Tetrazolio tetrazolio = new Tetrazolio();
        
        
        tetrazolio.setComentarios(solicitud.getComentarios());
        
        
        if (solicitud.getIdLote() != null) {
            System.out.println("Buscando lote con ID: " + solicitud.getIdLote());
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                Lote lote = loteOpt.get();
                
                
                if (!lote.getActivo()) {
                    throw new RuntimeException("No se puede crear un análisis para un lote inactivo");
                }
                
                tetrazolio.setLote(lote);
                System.out.println("Lote encontrado y asignado: " + lote.getLoteID());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }
        
        
        tetrazolio.setNumSemillasPorRep(solicitud.getNumSemillasPorRep());
        tetrazolio.setPretratamiento(solicitud.getPretratamiento());
        tetrazolio.setConcentracion(solicitud.getConcentracion());
        tetrazolio.setTincionHs(solicitud.getTincionHs());
        tetrazolio.setTincionTemp(solicitud.getTincionTemp());
        tetrazolio.setFecha(solicitud.getFecha());
        tetrazolio.setNumRepeticionesEsperadas(solicitud.getNumRepeticionesEsperadas());
        tetrazolio.setViabilidadInase(solicitud.getViabilidadInase());
        System.out.println("Viabilidad INASE asignada: " + solicitud.getViabilidadInase());
        
        System.out.println("Tetrazolio mapeado exitosamente");
        return tetrazolio;
    }

    
    private void actualizarEntidadDesdeSolicitud(Tetrazolio tetrazolio, TetrazolioRequestDTO solicitud) {
        System.out.println("Actualizando tetrazolio desde solicitud");
        
        
        tetrazolio.setComentarios(solicitud.getComentarios());
        
        
        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                tetrazolio.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }
        
        
        tetrazolio.setNumSemillasPorRep(solicitud.getNumSemillasPorRep());
        tetrazolio.setPretratamiento(solicitud.getPretratamiento());
        tetrazolio.setConcentracion(solicitud.getConcentracion());
        tetrazolio.setTincionHs(solicitud.getTincionHs());
        tetrazolio.setTincionTemp(solicitud.getTincionTemp());
        tetrazolio.setFecha(solicitud.getFecha());
        tetrazolio.setViabilidadInase(solicitud.getViabilidadInase());
        
        
        System.out.println("Tetrazolio actualizado exitosamente");
    }

    
    private TetrazolioDTO mapearEntidadADTO(Tetrazolio tetrazolio) {
        TetrazolioDTO dto = new TetrazolioDTO();
        
        
        dto.setAnalisisID(tetrazolio.getAnalisisID());
        dto.setEstado(tetrazolio.getEstado());
        dto.setFechaInicio(tetrazolio.getFechaInicio());
        dto.setFechaFin(tetrazolio.getFechaFin());
        dto.setComentarios(tetrazolio.getComentarios());
        
        
        if (tetrazolio.getLote() != null) {
            dto.setIdLote(tetrazolio.getLote().getLoteID());
            dto.setLote(tetrazolio.getLote().getNomLote());
            dto.setFicha(tetrazolio.getLote().getFicha());
            
            
            if (tetrazolio.getLote().getCultivar() != null) {
                dto.setCultivarNombre(tetrazolio.getLote().getCultivar().getNombre());
                
                if (tetrazolio.getLote().getCultivar().getEspecie() != null) {
                    dto.setEspecieNombre(tetrazolio.getLote().getCultivar().getEspecie().getNombreComun());
                }
            }
        }
        
        
        dto.setNumSemillasPorRep(tetrazolio.getNumSemillasPorRep());
        dto.setPretratamiento(tetrazolio.getPretratamiento());
        dto.setConcentracion(tetrazolio.getConcentracion());
        dto.setTincionHs(tetrazolio.getTincionHs());
        dto.setTincionTemp(tetrazolio.getTincionTemp());
        dto.setFecha(tetrazolio.getFecha());
        dto.setNumRepeticionesEsperadas(tetrazolio.getNumRepeticionesEsperadas());
        dto.setPorcViablesRedondeo(tetrazolio.getPorcViablesRedondeo());
        dto.setPorcNoViablesRedondeo(tetrazolio.getPorcNoViablesRedondeo());
        dto.setPorcDurasRedondeo(tetrazolio.getPorcDurasRedondeo());
        dto.setViabilidadInase(tetrazolio.getViabilidadInase());
        
        dto.setHistorial(analisisHistorialService.obtenerHistorialAnalisis(tetrazolio.getAnalisisID()));
        
        return dto;
    }
    
    
    private void validarCompletitudRepeticiones(Tetrazolio tetrazolio) {
        if (tetrazolio.getNumRepeticionesEsperadas() != null && tetrazolio.getNumRepeticionesEsperadas() > 0) {
            Long repeticionesCreadas = repeticionRepository.countByTetrazolioId(tetrazolio.getAnalisisID());
            
            if (repeticionesCreadas < tetrazolio.getNumRepeticionesEsperadas()) {
                throw new RuntimeException(
                    String.format("No se puede finalizar el análisis. Se esperan %d repeticiones pero solo hay %d creadas. " +
                                "Complete todas las repeticiones antes de finalizar o actualizar porcentajes.", 
                                tetrazolio.getNumRepeticionesEsperadas(), repeticionesCreadas));
            }
        }
    }

    
    private void validarEvidenciaAntesDeFinalizar(Tetrazolio tetrazolio) {
    boolean tieneRepeticiones = tetrazolio.getRepeticiones() != null && !tetrazolio.getRepeticiones().isEmpty();

    boolean tienePorcViables = tetrazolio.getPorcViablesRedondeo() != null
        && tetrazolio.getPorcViablesRedondeo().compareTo(BigDecimal.ZERO) > 0;
    boolean tienePorcNoViables = tetrazolio.getPorcNoViablesRedondeo() != null
        && tetrazolio.getPorcNoViablesRedondeo().compareTo(BigDecimal.ZERO) > 0;
    boolean tienePorcDuras = tetrazolio.getPorcDurasRedondeo() != null
        && tetrazolio.getPorcDurasRedondeo().compareTo(BigDecimal.ZERO) > 0;

    if (!tieneRepeticiones && !tienePorcViables && !tienePorcNoViables && !tienePorcDuras) {
        throw new RuntimeException("No se puede finalizar: el Tetrazolio carece de evidencia. Agregue repeticiones o porcentajes calculados antes de finalizar.");
    }
    }
    
    
    
    public TetrazolioDTO finalizarAnalisis(Long id) {
        
        return analisisService.finalizarAnalisisGenerico(
            id,
            tetrazolioRepository,
            this::mapearEntidadADTO,
            (tetrazolio) -> {
                
                this.validarCompletitudRepeticiones(tetrazolio);
                
                this.validarEvidenciaAntesDeFinalizar(tetrazolio);
            }
        );
    }

    
    public TetrazolioDTO aprobarAnalisis(Long id) {
        return analisisService.aprobarAnalisisGenerico(
            id,
            tetrazolioRepository,
            this::mapearEntidadADTO,
            (tetrazolio) -> {
                this.validarCompletitudRepeticiones(tetrazolio);
                this.validarEvidenciaAntesDeFinalizar(tetrazolio);
            },
            tetrazolioRepository::findByIdLote 
        );
    }

    
    public TetrazolioDTO marcarParaRepetir(Long id) {
        return analisisService.marcarParaRepetirGenerico(
            id,
            tetrazolioRepository,
            this::mapearEntidadADTO,
            (tetrazolio) -> {
                
                this.validarCompletitudRepeticiones(tetrazolio);
                this.validarEvidenciaAntesDeFinalizar(tetrazolio);
            }
        );
    }
}