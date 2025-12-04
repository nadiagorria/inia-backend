package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.business.mappers.MappingUtils;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ListadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.specifications.PurezaSpecification;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PurezaRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoPureza;

@Service
public class PurezaService {

    @Autowired
    private PurezaRepository purezaRepository;

    @Autowired
    private ListadoRepository listadoRepository;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @Autowired
    private MalezasCatalogoRepository malezasCatalogoRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

    
    @Transactional
    public PurezaDTO crearPureza(PurezaRequestDTO solicitud) {
        
        validarPesos(solicitud.getPesoInicial_g(), solicitud.getPesoTotal_g());
        
        Pureza pureza = mapearSolicitudAEntidad(solicitud);
        pureza.setEstado(Estado.EN_PROCESO);
        
        
        analisisService.establecerFechaInicio(pureza);
        
        Pureza purezaGuardada = purezaRepository.save(pureza);
        
        
        analisisHistorialService.registrarCreacion(purezaGuardada);
        
        return mapearEntidadADTO(purezaGuardada);
    }

    
    @Transactional
    public PurezaDTO actualizarPureza(Long id, PurezaRequestDTO solicitud) {
        Pureza pureza = purezaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pureza no encontrada con id: " + id));

        
        Estado estadoOriginal = pureza.getEstado();
        
        if (estadoOriginal == Estado.APROBADO && analisisService.esAnalista()) {
            
            pureza.setEstado(Estado.PENDIENTE_APROBACION);
        }
        
        

        
        BigDecimal pesoInicial = solicitud.getPesoInicial_g() != null ? solicitud.getPesoInicial_g() : pureza.getPesoInicial_g();
        BigDecimal pesoTotal = solicitud.getPesoTotal_g() != null ? solicitud.getPesoTotal_g() : pureza.getPesoTotal_g();
        validarPesos(pesoInicial, pesoTotal);

        actualizarEntidadDesdeSolicitud(pureza, solicitud);
        Pureza purezaActualizada = purezaRepository.save(pureza);
        
        
        analisisHistorialService.registrarModificacion(purezaActualizada);
        
        return mapearEntidadADTO(purezaActualizada);
    }

    
    public void desactivarPureza(Long id) {
        analisisService.desactivarAnalisis(id, purezaRepository);
    }

    
    public PurezaDTO reactivarPureza(Long id) {
        return analisisService.reactivarAnalisis(id, purezaRepository, this::mapearEntidadADTO);
    }

    
    public PurezaDTO obtenerPurezaPorId(Long id) {
        return purezaRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElseThrow(() -> new RuntimeException("Pureza no encontrada con id: " + id));
    }

    
    public List<PurezaDTO> obtenerPurezasPorIdLote(Long idLote) {
        return purezaRepository.findByIdLote(idLote)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }


    
    public Page<PurezaListadoDTO> obtenerPurezaPaginadasConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            String estado,
            Long loteId) {
        
        
        Specification<Pureza> spec = PurezaSpecification.conFiltros(searchTerm, activo, estado, loteId);
        
        
        Page<Pureza> purezaPage = purezaRepository.findAll(spec, pageable);
        
        
        return purezaPage.map(this::mapearEntidadAListadoDTO);
    }

    
    private PurezaListadoDTO mapearEntidadAListadoDTO(Pureza pureza) {
        PurezaListadoDTO dto = new PurezaListadoDTO();
        dto.setAnalisisID(pureza.getAnalisisID());
        dto.setEstado(pureza.getEstado());
        dto.setFechaInicio(pureza.getFechaInicio());
        dto.setFechaFin(pureza.getFechaFin());
        dto.setActivo(pureza.getActivo());
        
        
        dto.setRedonSemillaPura(pureza.getRedonSemillaPura());
        dto.setInasePura(pureza.getInasePura());
        
        if (pureza.getLote() != null) {
            dto.setIdLote(pureza.getLote().getLoteID());
            dto.setLote(pureza.getLote().getNomLote()); 
            
            
            if (pureza.getLote().getCultivar() != null && pureza.getLote().getCultivar().getEspecie() != null) {
                String nombreEspecie = pureza.getLote().getCultivar().getEspecie().getNombreComun();
                
                if (nombreEspecie == null || nombreEspecie.trim().isEmpty()) {
                    nombreEspecie = pureza.getLote().getCultivar().getEspecie().getNombreCientifico();
                }
                dto.setEspecie(nombreEspecie);
            }
        }
        
        if (pureza.getAnalisisID() != null) {
            var historial = analisisHistorialService.obtenerHistorialAnalisis(pureza.getAnalisisID());
            if (!historial.isEmpty()) {
                var primerRegistro = historial.get(historial.size() - 1);
                dto.setUsuarioCreador(primerRegistro.getUsuario());
                var ultimoRegistro = historial.get(0);
                dto.setUsuarioModificador(ultimoRegistro.getUsuario());
            }
        }
        return dto;
    }


    

    private Pureza mapearSolicitudAEntidad(PurezaRequestDTO solicitud) {
        Pureza pureza = new Pureza();

        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                Lote lote = loteOpt.get();
                
                
                if (!lote.getActivo()) {
                    throw new RuntimeException("No se puede crear un análisis para un lote inactivo");
                }
                
                pureza.setLote(lote);
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        
        pureza.setCumpleEstandar(solicitud.getCumpleEstandar());
        pureza.setComentarios(solicitud.getComentarios());

        
        pureza.setFecha(solicitud.getFecha());
        pureza.setPesoInicial_g(solicitud.getPesoInicial_g());
        pureza.setSemillaPura_g(solicitud.getSemillaPura_g());
        pureza.setMateriaInerte_g(solicitud.getMateriaInerte_g());
        pureza.setOtrosCultivos_g(solicitud.getOtrosCultivos_g());
        pureza.setMalezas_g(solicitud.getMalezas_g());
        pureza.setMalezasToleradas_g(solicitud.getMalezasToleradas_g());
        pureza.setMalezasTolCero_g(solicitud.getMalezasTolCero_g());
        pureza.setPesoTotal_g(solicitud.getPesoTotal_g());

        pureza.setRedonSemillaPura(solicitud.getRedonSemillaPura());
        pureza.setRedonMateriaInerte(solicitud.getRedonMateriaInerte());
        pureza.setRedonOtrosCultivos(solicitud.getRedonOtrosCultivos());
        pureza.setRedonMalezas(solicitud.getRedonMalezas());
        pureza.setRedonMalezasToleradas(solicitud.getRedonMalezasToleradas());
        pureza.setRedonMalezasTolCero(solicitud.getRedonMalezasTolCero());
        pureza.setRedonPesoTotal(solicitud.getRedonPesoTotal());

        pureza.setInasePura(solicitud.getInasePura());
        pureza.setInaseMateriaInerte(solicitud.getInaseMateriaInerte());
        pureza.setInaseOtrosCultivos(solicitud.getInaseOtrosCultivos());
        pureza.setInaseMalezas(solicitud.getInaseMalezas());
        pureza.setInaseMalezasToleradas(solicitud.getInaseMalezasToleradas());
        pureza.setInaseMalezasTolCero(solicitud.getInaseMalezasTolCero());
        pureza.setInaseFecha(solicitud.getInaseFecha());

        if (solicitud.getOtrasSemillas() != null && !solicitud.getOtrasSemillas().isEmpty()) {
            List<Listado> otrasSemillas = solicitud.getOtrasSemillas().stream()
                    .map(req -> crearListadoDesdeSolicitud(req, pureza))
                    .collect(Collectors.toList());
            pureza.setListados(otrasSemillas);
        }

        return pureza;
    }

    private void actualizarEntidadDesdeSolicitud(Pureza pureza, PurezaRequestDTO solicitud) {
        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                pureza.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }


        if (solicitud.getCumpleEstandar() != null) pureza.setCumpleEstandar(solicitud.getCumpleEstandar());
        if (solicitud.getComentarios() != null) pureza.setComentarios(solicitud.getComentarios());

        if (solicitud.getFecha() != null) pureza.setFecha(solicitud.getFecha());
        if (solicitud.getPesoInicial_g() != null) pureza.setPesoInicial_g(solicitud.getPesoInicial_g());
        if (solicitud.getSemillaPura_g() != null) pureza.setSemillaPura_g(solicitud.getSemillaPura_g());
        if (solicitud.getMateriaInerte_g() != null) pureza.setMateriaInerte_g(solicitud.getMateriaInerte_g());
        if (solicitud.getOtrosCultivos_g() != null) pureza.setOtrosCultivos_g(solicitud.getOtrosCultivos_g());
        if (solicitud.getMalezas_g() != null) pureza.setMalezas_g(solicitud.getMalezas_g());
        if (solicitud.getMalezasToleradas_g() != null) pureza.setMalezasToleradas_g(solicitud.getMalezasToleradas_g());
        if (solicitud.getMalezasTolCero_g() != null) pureza.setMalezasTolCero_g(solicitud.getMalezasTolCero_g());
        if (solicitud.getPesoTotal_g() != null) pureza.setPesoTotal_g(solicitud.getPesoTotal_g());

        if (solicitud.getRedonSemillaPura() != null) pureza.setRedonSemillaPura(solicitud.getRedonSemillaPura());
        if (solicitud.getRedonMateriaInerte() != null) pureza.setRedonMateriaInerte(solicitud.getRedonMateriaInerte());
        if (solicitud.getRedonOtrosCultivos() != null) pureza.setRedonOtrosCultivos(solicitud.getRedonOtrosCultivos());
        if (solicitud.getRedonMalezas() != null) pureza.setRedonMalezas(solicitud.getRedonMalezas());
        if (solicitud.getRedonMalezasToleradas() != null) pureza.setRedonMalezasToleradas(solicitud.getRedonMalezasToleradas());
        if (solicitud.getRedonMalezasTolCero() != null) pureza.setRedonMalezasTolCero(solicitud.getRedonMalezasTolCero());
        if (solicitud.getRedonPesoTotal() != null) pureza.setRedonPesoTotal(solicitud.getRedonPesoTotal());

        if (solicitud.getInasePura() != null) pureza.setInasePura(solicitud.getInasePura());
        if (solicitud.getInaseMateriaInerte() != null) pureza.setInaseMateriaInerte(solicitud.getInaseMateriaInerte());
        if (solicitud.getInaseOtrosCultivos() != null) pureza.setInaseOtrosCultivos(solicitud.getInaseOtrosCultivos());
        if (solicitud.getInaseMalezas() != null) pureza.setInaseMalezas(solicitud.getInaseMalezas());
        if (solicitud.getInaseMalezasToleradas() != null) pureza.setInaseMalezasToleradas(solicitud.getInaseMalezasToleradas());
        if (solicitud.getInaseMalezasTolCero() != null) pureza.setInaseMalezasTolCero(solicitud.getInaseMalezasTolCero());
        if (solicitud.getInaseFecha() != null) pureza.setInaseFecha(solicitud.getInaseFecha());

        if (solicitud.getOtrasSemillas() != null) {
            
            if (pureza.getListados() == null) {
                pureza.setListados(new ArrayList<>());
            }
            
            
            pureza.getListados().clear();

            
            if (!solicitud.getOtrasSemillas().isEmpty()) {
                List<Listado> nuevosListados = solicitud.getOtrasSemillas().stream()
                        .map(req -> crearListadoDesdeSolicitud(req, pureza))
                        .collect(Collectors.toList());
                
                pureza.getListados().addAll(nuevosListados);
            }
        }
    }

    private PurezaDTO mapearEntidadADTO(Pureza pureza) {
        PurezaDTO dto = new PurezaDTO();

        dto.setAnalisisID(pureza.getAnalisisID());
        dto.setEstado(pureza.getEstado());
        dto.setFechaInicio(pureza.getFechaInicio());
        dto.setFechaFin(pureza.getFechaFin());
        dto.setCumpleEstandar(pureza.getCumpleEstandar());
        dto.setComentarios(pureza.getComentarios());
        
        
        if (pureza.getLote() != null) {
            dto.setIdLote(pureza.getLote().getLoteID());
            dto.setLote(pureza.getLote().getNomLote());
            dto.setFicha(pureza.getLote().getFicha());
            
            
            if (pureza.getLote().getCultivar() != null) {
                dto.setCultivarNombre(pureza.getLote().getCultivar().getNombre());
                
                if (pureza.getLote().getCultivar().getEspecie() != null) {
                    dto.setEspecieNombre(pureza.getLote().getCultivar().getEspecie().getNombreComun());
                }
            }
        }

        dto.setFecha(pureza.getFecha());
        dto.setPesoInicial_g(pureza.getPesoInicial_g());
        dto.setSemillaPura_g(pureza.getSemillaPura_g());
        dto.setMateriaInerte_g(pureza.getMateriaInerte_g());
        dto.setOtrosCultivos_g(pureza.getOtrosCultivos_g());
        dto.setMalezas_g(pureza.getMalezas_g());
        dto.setMalezasToleradas_g(pureza.getMalezasToleradas_g());
        dto.setMalezasTolCero_g(pureza.getMalezasTolCero_g());
        dto.setPesoTotal_g(pureza.getPesoTotal_g());

        dto.setRedonSemillaPura(pureza.getRedonSemillaPura());
        dto.setRedonMateriaInerte(pureza.getRedonMateriaInerte());
        dto.setRedonOtrosCultivos(pureza.getRedonOtrosCultivos());
        dto.setRedonMalezas(pureza.getRedonMalezas());
        dto.setRedonMalezasToleradas(pureza.getRedonMalezasToleradas());
        dto.setRedonMalezasTolCero(pureza.getRedonMalezasTolCero());
        dto.setRedonPesoTotal(pureza.getRedonPesoTotal());

        dto.setInasePura(pureza.getInasePura());
        dto.setInaseMateriaInerte(pureza.getInaseMateriaInerte());
        dto.setInaseOtrosCultivos(pureza.getInaseOtrosCultivos());
        dto.setInaseMalezas(pureza.getInaseMalezas());
        dto.setInaseMalezasToleradas(pureza.getInaseMalezasToleradas());
        dto.setInaseMalezasTolCero(pureza.getInaseMalezasTolCero());
        dto.setInaseFecha(pureza.getInaseFecha());

        if (pureza.getListados() != null) {
            List<ListadoDTO> otrasSemillasDTO = pureza.getListados().stream()
                    .map(MappingUtils::toListadoDTO)
                    .collect(Collectors.toList());
            dto.setOtrasSemillas(otrasSemillasDTO);
        }

        
        dto.setHistorial(analisisHistorialService.obtenerHistorialAnalisis(pureza.getAnalisisID()));

        return dto;
    }

    private Listado crearListadoDesdeSolicitud(ListadoRequestDTO solicitud, Pureza pureza) {
        Listado listado = MappingUtils.fromListadoRequest(solicitud, malezasCatalogoRepository, especieRepository);
        listado.setPureza(pureza);
        return listado;
    }

    
    private void validarPesos(BigDecimal pesoInicial_g, BigDecimal pesoTotal_g) {
        if (pesoInicial_g == null || pesoTotal_g == null) {
            return; 
        }

        
        if (pesoInicial_g.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El peso inicial debe ser mayor a cero para realizar el análisis");
        }

        
        if (pesoTotal_g.compareTo(pesoInicial_g) > 0) {
            throw new RuntimeException("El peso total (" + pesoTotal_g + "g) no puede ser mayor al peso inicial (" + pesoInicial_g + "g)");
        }

        
        
        BigDecimal diferenciaPeso = pesoInicial_g.subtract(pesoTotal_g);
        BigDecimal porcentajePerdida = diferenciaPeso.divide(pesoInicial_g, 4, java.math.RoundingMode.HALF_UP)
                                                    .multiply(new BigDecimal("100"));
        
        BigDecimal limitePermitido = new BigDecimal("5.0");
        if (porcentajePerdida.compareTo(limitePermitido) > 0) {
            
            System.out.println("INFO: La muestra ha perdido " + porcentajePerdida.setScale(2, java.math.RoundingMode.HALF_UP) + 
                             "% de su peso inicial, lo cual excede el límite recomendado del 5%. " +
                             "Pérdida: " + diferenciaPeso.setScale(2, java.math.RoundingMode.HALF_UP) + "g");
        }
    }

    
    public PurezaDTO finalizarAnalisis(Long id) {
        return analisisService.finalizarAnalisisGenerico(
            id,
            purezaRepository,
            this::mapearEntidadADTO,
            this::validarAntesDeFinalizar
        );
    }

    
    private void validarAntesDeFinalizar(Pureza pureza) {
        
        boolean tieneDatosRedon = pureza.getRedonSemillaPura() != null
                || pureza.getRedonMateriaInerte() != null
                || pureza.getRedonOtrosCultivos() != null
                || pureza.getRedonMalezas() != null
                || pureza.getRedonMalezasToleradas() != null
                || pureza.getRedonMalezasTolCero() != null;

        
        boolean tieneDatosINASE = (pureza.getInaseFecha() != null)
                && (pureza.getInasePura() != null
                    || pureza.getInaseMateriaInerte() != null
                    || pureza.getInaseOtrosCultivos() != null
                    || pureza.getInaseMalezas() != null
                    || pureza.getInaseMalezasToleradas() != null
                    || pureza.getInaseMalezasTolCero() != null);

        
        boolean tieneListados = pureza.getListados() != null && !pureza.getListados().isEmpty();

        
        if (!tieneDatosRedon && !tieneDatosINASE && !tieneListados) {
            throw new RuntimeException("No se puede finalizar: el análisis de Pureza carece de evidencia. Agregue datos de pesos (Redon), datos INASE o listados antes de finalizar.");
        }

        
        if (pureza.getPesoInicial_g() != null && pureza.getPesoInicial_g().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El peso inicial debe ser mayor que 0");
        }
        if (pureza.getPesoTotal_g() != null && pureza.getPesoTotal_g().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El peso total debe ser mayor que 0");
        }
    }

    
    public PurezaDTO aprobarAnalisis(Long id) {
        return analisisService.aprobarAnalisisGenerico(
            id,
            purezaRepository,
            this::mapearEntidadADTO,
            this::validarAntesDeFinalizar,
            purezaRepository::findByIdLote 
        );
    }

    
    public PurezaDTO marcarParaRepetir(Long id) {
        return analisisService.marcarParaRepetirGenerico(
            id,
            purezaRepository,
            this::mapearEntidadADTO,
            this::validarAntesDeFinalizar
        );
    }
}
