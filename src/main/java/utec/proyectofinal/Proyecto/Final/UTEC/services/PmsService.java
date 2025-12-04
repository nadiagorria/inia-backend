package utec.proyectofinal.Proyecto.Final.UTEC.services;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepPmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.specifications.PmsSpecification;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EstadisticasTandaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsListadoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Service
public class PmsService {

    @Autowired
    private PmsRepository pmsRepository;

    @Autowired
    private RepPmsRepository repPmsRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

        @Transactional
    
    public PmsDTO crearPms(PmsRequestDTO solicitud) {
        
        if (solicitud.getNumRepeticionesEsperadas() == null || solicitud.getNumRepeticionesEsperadas() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de repeticiones esperadas (mayor a 0).");
        }
        
        
        
        if (solicitud.getNumRepeticionesEsperadas() > 16) {
            throw new RuntimeException("El número de repeticiones por tanda no puede superar 16.");
        }
        
        Pms pms = mapearSolicitudAEntidad(solicitud);
        pms.setEstado(Estado.REGISTRADO);

        Pms guardado = pmsRepository.save(pms);
        
        
        analisisHistorialService.registrarCreacion(guardado);
        
        return mapearEntidadADTO(guardado);
    }

    
    @Transactional
    public PmsDTO actualizarPms(Long id, PmsRequestDTO solicitud) {
        Optional<Pms> existente = pmsRepository.findById(id);

        if (existente.isPresent()) {
            Pms pms = existente.get();
            
            
            analisisService.manejarEdicionAnalisisFinalizado(pms);
            
            actualizarEntidadDesdeSolicitud(pms, solicitud);
            Pms actualizado = pmsRepository.save(pms);
            
            
            analisisHistorialService.registrarModificacion(actualizado);
            
            return mapearEntidadADTO(actualizado);
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    
    public void eliminarPms(Long id) {
        Optional<Pms> existente = pmsRepository.findById(id);

        if (existente.isPresent()) {
            Pms pms = existente.get();
            pms.setActivo(false);
            pmsRepository.save(pms);
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    
    public void desactivarPms(Long id) {
        analisisService.desactivarAnalisis(id, pmsRepository);
    }

    
    public PmsDTO reactivarPms(Long id) {
        return analisisService.reactivarAnalisis(id, pmsRepository, this::mapearEntidadADTO);
    }

    
    public List<PmsDTO> obtenerTodos() {
        List<Pms> activos = pmsRepository.findByActivoTrue();

        return activos.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public PmsDTO obtenerPorId(Long id) {
        Optional<Pms> pms = pmsRepository.findById(id);
        if (pms.isPresent()) {
            return mapearEntidadADTO(pms.get());
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    public List<PmsDTO> obtenerPmsPorIdLote(Long idLote) {
        List<Pms> lista = pmsRepository.findByIdLote(idLote.intValue());

        return lista.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public Page<PmsListadoDTO> obtenerPmsPaginadas(Pageable pageable) {
        Page<Pms> pmsPage = pmsRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
        return pmsPage.map(this::mapearEntidadAListadoDTO);
    }

    
    public Page<PmsListadoDTO> obtenerPmsPaginadasConFiltro(Pageable pageable, String filtroActivo) {
        Page<Pms> pmsPage;
        
        switch (filtroActivo.toLowerCase()) {
            case "activos":
                pmsPage = pmsRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
                break;
            case "inactivos":
                pmsPage = pmsRepository.findByActivoFalseOrderByFechaInicioDesc(pageable);
                break;
            default: 
                pmsPage = pmsRepository.findAllByOrderByFechaInicioDesc(pageable);
                break;
        }
        
        return pmsPage.map(this::mapearEntidadAListadoDTO);
    }

    /**
     * Listar PMS con paginado y filtros dinámicos
     */
    public Page<PmsListadoDTO> obtenerPmsPaginadasConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            String estado,
            Long loteId) {
        
        Specification<Pms> spec = PmsSpecification.conFiltros(searchTerm, activo, estado, loteId);
        Page<Pms> pmsPage = pmsRepository.findAll(spec, pageable);
        return pmsPage.map(this::mapearEntidadAListadoDTO);
    }

    private PmsListadoDTO mapearEntidadAListadoDTO(Pms pms) {
        PmsListadoDTO dto = new PmsListadoDTO();
        dto.setAnalisisID(pms.getAnalisisID());
        dto.setEstado(pms.getEstado());
        dto.setFechaInicio(pms.getFechaInicio());
        dto.setFechaFin(pms.getFechaFin());
        dto.setActivo(pms.getActivo());
        
        
        dto.setPms_g(pms.getPmsconRedon());
        dto.setCoeficienteVariacion(pms.getCoefVariacion());
        
        if (pms.getLote() != null) {
            dto.setIdLote(pms.getLote().getLoteID());
            dto.setLote(pms.getLote().getNomLote()); 
            
            
            if (pms.getLote().getCultivar() != null && pms.getLote().getCultivar().getEspecie() != null) {
                String nombreEspecie = pms.getLote().getCultivar().getEspecie().getNombreComun();
                
                if (nombreEspecie == null || nombreEspecie.trim().isEmpty()) {
                    nombreEspecie = pms.getLote().getCultivar().getEspecie().getNombreCientifico();
                }
                dto.setEspecie(nombreEspecie);
            }
        }
        
        if (pms.getAnalisisID() != null) {
            var historial = analisisHistorialService.obtenerHistorialAnalisis(pms.getAnalisisID());
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
    public PmsDTO actualizarPmsConRedondeo(Long id, PmsRedondeoRequestDTO solicitud) {
        Optional<Pms> pmsExistente = pmsRepository.findById(id);
        
        if (pmsExistente.isPresent()) {
            Pms pms = pmsExistente.get();
            
            System.out.println("=== DEBUG actualizarPmsConRedondeo ===");
            System.out.println("PMS ID: " + id);
            System.out.println("Estado actual: " + pms.getEstado());
            System.out.println("Número de tandas: " + pms.getNumTandas());
            System.out.println("Repeticiones esperadas: " + pms.getNumRepeticionesEsperadas());
            
            
            if (pms.getEstado() != Estado.EN_PROCESO && 
                pms.getEstado() != Estado.PENDIENTE_APROBACION && 
                pms.getEstado() != Estado.APROBADO) {
                System.out.println("ERROR: Estado no válido: " + pms.getEstado());
                throw new RuntimeException("Solo se pueden actualizar valores finales de PMS en estado EN_PROCESO, PENDIENTE_APROBACION o APROBADO. Estado actual: " + pms.getEstado());
            }
            
            
            boolean repeticionesCompletas = todasLasRepeticionesCompletas(pms);
            System.out.println("Repeticiones completas: " + repeticionesCompletas);
            if (!repeticionesCompletas) {
                System.out.println("ERROR: No todas las repeticiones están completas");
                throw new RuntimeException("No se pueden actualizar los valores finales hasta completar todas las repeticiones válidas");
            }
            
            
            pms.setPmsconRedon(solicitud.getPmsconRedon());
            
            Pms pmsActualizado = pmsRepository.save(pms);
            System.out.println("PMS con redondeo actualizado exitosamente para PMS ID: " + id);
            return mapearEntidadADTO(pmsActualizado);
        } else {
            throw new RuntimeException("Análisis de PMS no encontrado con ID: " + id);
        }
    }

    
    public void procesarCalculosTanda(Long pmsId, Integer numTanda) {
        Pms pms = pmsRepository.findById(pmsId)
            .orElseThrow(() -> new RuntimeException("PMS no encontrado con ID: " + pmsId));
        
        List<RepPms> repeticionesTanda = repPmsRepository.findByPmsId(pmsId).stream()
            .filter(rep -> rep.getNumTanda().equals(numTanda))
            .collect(Collectors.toList());
        
        if (repeticionesTanda.size() < pms.getNumRepeticionesEsperadas()) {
            
            actualizarEstadisticasGenerales(pms);
            pmsRepository.save(pms);
            return;
        }
        
        
        List<RepPms> todasLasRepeticiones = repPmsRepository.findByPmsId(pmsId);
        
        
        EstadisticasTandaDTO estadisticasGlobales = calcularEstadisticasTanda(todasLasRepeticiones);
        
        
        BigDecimal media = estadisticasGlobales.getPromedio();
        BigDecimal desviacion = estadisticasGlobales.getDesviacion();
        BigDecimal umbralInferior = media.subtract(desviacion.multiply(new BigDecimal("2")));
        BigDecimal umbralSuperior = media.add(desviacion.multiply(new BigDecimal("2")));
        
        System.out.println(" ESTADÍSTICAS GLOBALES DEL PMS (todas las repeticiones):");
        System.out.println("  Total repeticiones: " + todasLasRepeticiones.size());
        System.out.println("  Media (μ): " + media);
        System.out.println("  Desviación (σ): " + desviacion);
        System.out.println("  Umbral inferior (μ - 2σ): " + umbralInferior);
        System.out.println("  Umbral superior (μ + 2σ): " + umbralSuperior);
        System.out.println(" VALIDACIÓN DE TANDA " + numTanda + ":");
        
        
        for (RepPms rep : repeticionesTanda) {
            boolean esValida = rep.getPeso().compareTo(umbralInferior) >= 0 && 
                              rep.getPeso().compareTo(umbralSuperior) <= 0;
            rep.setValido(esValida);
            System.out.println("    Rep #" + rep.getNumRep() + " (Tanda " + numTanda + "): " + rep.getPeso() + "g -> " + 
                (esValida ? " VÁLIDA" : " INVÁLIDA") +
                " (comparación: " + rep.getPeso() + " vs [" + umbralInferior + ", " + umbralSuperior + "])");
        }
        repPmsRepository.saveAll(repeticionesTanda);
        System.out.println("   Validaciones guardadas en BD");
        
        
        List<RepPms> repeticionesValidas = repeticionesTanda.stream()
            .filter(rep -> Boolean.TRUE.equals(rep.getValido()))
            .collect(Collectors.toList());
        
        if (repeticionesValidas.isEmpty()) {
            
            if (puedeIncrementarTandas(pms)) {
                pms.setNumTandas(pms.getNumTandas() + 1);
                System.out.println("No hay repeticiones válidas en tanda " + numTanda + ". Se incrementa el número de tandas a: " + pms.getNumTandas());
            }
            actualizarEstadisticasGenerales(pms);
            pmsRepository.save(pms);
            return;
        }
        
        
        EstadisticasTandaDTO estadisticasTanda = calcularEstadisticasTanda(repeticionesValidas);
        
        
        BigDecimal umbralCV = pms.getEsSemillaBrozosa() ? 
            new BigDecimal("6.0") : new BigDecimal("4.0");
        
        System.out.println("  CV de la tanda " + numTanda + ": " + estadisticasTanda.getCoeficienteVariacion() + " (umbral: " + umbralCV + ")");
        
        if (estadisticasTanda.getCoeficienteVariacion().compareTo(umbralCV) > 0) {
            
            if (puedeIncrementarTandas(pms)) {
                pms.setNumTandas(pms.getNumTandas() + 1);
                System.out.println("   CV no aceptable. Se incrementa el número de tandas a: " + pms.getNumTandas());
            } else {
                System.out.println("   CV no aceptable pero se alcanzó el límite máximo de 16 repeticiones.");
            }
        } else {
            System.out.println("   CV aceptable para la tanda " + numTanda);
        }
        
        
        actualizarEstadisticasGenerales(pms);
        
        pmsRepository.save(pms);
    }
    
    /**
     * Valida TODAS las repeticiones del PMS usando estadísticas globales (±2σ).
     * Se ejecuta cada vez que se crea, edita o elimina una repetición.
     * No considera tandas - simplemente valida todas las repeticiones contra la media global.
     */
    @Transactional
    public void validarTodasLasRepeticiones(Long pmsId) {
        System.out.println(" VALIDANDO TODAS LAS REPETICIONES del PMS ID: " + pmsId);
        
        Pms pms = pmsRepository.findById(pmsId)
            .orElseThrow(() -> new RuntimeException("PMS no encontrado con ID: " + pmsId));
        
        
        List<RepPms> todasLasRepeticiones = repPmsRepository.findByPmsId(pmsId);
        
        if (todasLasRepeticiones.isEmpty()) {
            System.out.println("  No hay repeticiones para validar");
            return;
        }
        
        
        if (todasLasRepeticiones.size() < pms.getNumRepeticionesEsperadas()) {
            System.out.println("  Repeticiones insuficientes (" + todasLasRepeticiones.size() + " < " + pms.getNumRepeticionesEsperadas() + "), marcando todas como indeterminadas");
            for (RepPms rep : todasLasRepeticiones) {
                rep.setValido(null);
            }
            repPmsRepository.saveAll(todasLasRepeticiones);
            actualizarEstadisticasGenerales(pms);
            pmsRepository.save(pms);
            return;
        }
        
        
        EstadisticasTandaDTO estadisticasGlobales = calcularEstadisticasTanda(todasLasRepeticiones);
        BigDecimal media = estadisticasGlobales.getPromedio();
        BigDecimal desviacion = estadisticasGlobales.getDesviacion();
        BigDecimal umbralInferior = media.subtract(desviacion.multiply(new BigDecimal("2")));
        BigDecimal umbralSuperior = media.add(desviacion.multiply(new BigDecimal("2")));
        
        System.out.println(" ESTADÍSTICAS GLOBALES:");
        System.out.println("  Total repeticiones: " + todasLasRepeticiones.size());
        System.out.println("  Media (μ): " + media);
        System.out.println("  Desviación (σ): " + desviacion);
        System.out.println("  Umbral inferior (μ - 2σ): " + umbralInferior);
        System.out.println("  Umbral superior (μ + 2σ): " + umbralSuperior);
        System.out.println("  Validación:");
        
        
        for (RepPms rep : todasLasRepeticiones) {
            boolean esValida = rep.getPeso().compareTo(umbralInferior) >= 0 && 
                              rep.getPeso().compareTo(umbralSuperior) <= 0;
            rep.setValido(esValida);
            System.out.println("    Rep #" + rep.getNumRep() + " (Tanda " + rep.getNumTanda() + "): " + 
                rep.getPeso() + "g -> " + (esValida ? " VÁLIDA" : " INVÁLIDA"));
        }
        
        
        repPmsRepository.saveAll(todasLasRepeticiones);
        System.out.println("   Validaciones guardadas");
        
        
        List<RepPms> repeticionesValidas = todasLasRepeticiones.stream()
            .filter(rep -> Boolean.TRUE.equals(rep.getValido()))
            .collect(Collectors.toList());
        
        if (repeticionesValidas.size() >= pms.getNumRepeticionesEsperadas()) {
            EstadisticasTandaDTO estadisticasValidas = calcularEstadisticasTanda(repeticionesValidas);
            BigDecimal umbralCV = pms.getEsSemillaBrozosa() ? new BigDecimal("6.0") : new BigDecimal("4.0");
            
            System.out.println("  CV con repeticiones válidas: " + estadisticasValidas.getCoeficienteVariacion() + " (umbral: " + umbralCV + ")");
            
            if (estadisticasValidas.getCoeficienteVariacion().compareTo(umbralCV) > 0 && puedeIncrementarTandas(pms)) {
                pms.setNumTandas(pms.getNumTandas() + 1);
                System.out.println("   CV no aceptable. Se incrementa número de tandas a: " + pms.getNumTandas());
            }
        }
        
        
        actualizarEstadisticasGenerales(pms);
        pmsRepository.save(pms);
        
        System.out.println(" Validación completada");
    }
    
    
    public void actualizarEstadisticasPms(Long pmsId) {
        Pms pms = pmsRepository.findById(pmsId)
            .orElseThrow(() -> new RuntimeException("PMS no encontrado con ID: " + pmsId));
        
        actualizarEstadisticasGenerales(pms);
        pmsRepository.save(pms);
    }



    
    
    

    private Pms mapearSolicitudAEntidad(PmsRequestDTO solicitud) {
        Pms pms = new Pms();

        pms.setComentarios(solicitud.getComentarios());

        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                Lote lote = loteOpt.get();
                
                
                if (!lote.getActivo()) {
                    throw new RuntimeException("No se puede crear un análisis para un lote inactivo");
                }
                
                pms.setLote(lote);
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        
        pms.setNumRepeticionesEsperadas(solicitud.getNumRepeticionesEsperadas());
        pms.setNumTandas(1); 
        pms.setEsSemillaBrozosa(solicitud.getEsSemillaBrozosa());

        return pms;
    }

    private void actualizarEntidadDesdeSolicitud(Pms pms, PmsRequestDTO solicitud) {

        pms.setComentarios(solicitud.getComentarios());

        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                pms.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        
        
        
        pms.setEsSemillaBrozosa(solicitud.getEsSemillaBrozosa());
    }

    private PmsDTO mapearEntidadADTO(Pms pms) {
        PmsDTO dto = new PmsDTO();

        dto.setAnalisisID(pms.getAnalisisID());
        dto.setEstado(pms.getEstado());
        dto.setFechaInicio(pms.getFechaInicio());
        dto.setFechaFin(pms.getFechaFin());
        dto.setComentarios(pms.getComentarios());

        
        if (pms.getLote() != null) {
            dto.setIdLote(pms.getLote().getLoteID());
            dto.setLote(pms.getLote().getNomLote());
            dto.setFicha(pms.getLote().getFicha());
            
            
            if (pms.getLote().getCultivar() != null) {
                dto.setCultivarNombre(pms.getLote().getCultivar().getNombre());
                
                if (pms.getLote().getCultivar().getEspecie() != null) {
                    dto.setEspecieNombre(pms.getLote().getCultivar().getEspecie().getNombreComun());
                }
            }
        }

        
        dto.setNumRepeticionesEsperadas(pms.getNumRepeticionesEsperadas());
        dto.setNumTandas(pms.getNumTandas());
        dto.setEsSemillaBrozosa(pms.getEsSemillaBrozosa());
        
        
        dto.setPromedio100g(pms.getPromedio100g());
        dto.setDesvioStd(pms.getDesvioStd());
        dto.setCoefVariacion(pms.getCoefVariacion());
        dto.setPmssinRedon(pms.getPmssinRedon());
        dto.setPmsconRedon(pms.getPmsconRedon());

        
        dto.setHistorial(analisisHistorialService.obtenerHistorialAnalisis(pms.getAnalisisID()));

        return dto;
    }

    
    
    

    private EstadisticasTandaDTO calcularEstadisticasTanda(List<RepPms> repeticiones) {
        if (repeticiones.isEmpty()) {
            throw new RuntimeException("No se pueden calcular estadísticas de una tanda vacía");
        }

        
        BigDecimal suma = repeticiones.stream()
            .map(RepPms::getPeso)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal promedio = suma.divide(
            new BigDecimal(repeticiones.size()), 
            4, RoundingMode.HALF_UP  
        );

        
        BigDecimal sumaCuadrados = repeticiones.stream()
            .map(rep -> rep.getPeso().subtract(promedio).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        
        int n = repeticiones.size();
        BigDecimal divisor = n > 1 ? new BigDecimal(n - 1) : new BigDecimal(1);
        
        BigDecimal varianza = sumaCuadrados.divide(
            divisor, 
            MathContext.DECIMAL128
        );
        
        BigDecimal desviacion = new BigDecimal(Math.sqrt(varianza.doubleValue()))
            .setScale(4, RoundingMode.HALF_UP);

        
        BigDecimal coeficienteVariacion = desviacion
            .divide(promedio, MathContext.DECIMAL128)
            .multiply(new BigDecimal("100"))
            .setScale(4, RoundingMode.HALF_UP);  

        
        BigDecimal pmsSinRedondeo = promedio.multiply(new BigDecimal("10"))
            .setScale(4, RoundingMode.HALF_UP);  

        return new EstadisticasTandaDTO(promedio, desviacion, coeficienteVariacion, pmsSinRedondeo);
    }

    private void actualizarEstadisticasGenerales(Pms pms) {
        
        List<RepPms> repeticionesValidas = repPmsRepository.findByPmsId(pms.getAnalisisID()).stream()
            .filter(rep -> Boolean.TRUE.equals(rep.getValido()))
            .collect(Collectors.toList());
        
        if (!repeticionesValidas.isEmpty()) {
            EstadisticasTandaDTO estadisticasGenerales = calcularEstadisticasTanda(repeticionesValidas);
            
            
            pms.setPromedio100g(estadisticasGenerales.getPromedio());
            pms.setDesvioStd(estadisticasGenerales.getDesviacion());
            pms.setCoefVariacion(estadisticasGenerales.getCoeficienteVariacion());
            pms.setPmssinRedon(estadisticasGenerales.getPmsSinRedondeo());
        } else {
            
            pms.setPromedio100g(null);
            pms.setDesvioStd(null);
            pms.setCoefVariacion(null);
            pms.setPmssinRedon(null);
        }
    }

    private boolean todasLasTandasCompletas(Pms pms) {
        for (int tandaNum = 1; tandaNum <= pms.getNumTandas(); tandaNum++) {
            final int tanda = tandaNum;
            List<RepPms> repeticionesTanda = repPmsRepository.findByPmsId(pms.getAnalisisID()).stream()
                .filter(rep -> rep.getNumTanda().equals(tanda) && Boolean.TRUE.equals(rep.getValido()))
                .collect(Collectors.toList());
            
            if (repeticionesTanda.size() < pms.getNumRepeticionesEsperadas()) {
                return false;
            }
        }
        return true;
    }

    private boolean puedeIncrementarTandas(Pms pms) {
        long totalRepeticiones = repPmsRepository.countByPmsId(pms.getAnalisisID());
        
        return totalRepeticiones < 16;
    }

    private boolean todasLasRepeticionesCompletas(Pms pms) {
        System.out.println("=== DEBUG todasLasRepeticionesCompletas ===");
        
        
        List<RepPms> todasLasRepeticiones = repPmsRepository.findByPmsId(pms.getAnalisisID());
        long totalRepeticiones = todasLasRepeticiones.size();
        
        System.out.println("Total de repeticiones: " + totalRepeticiones);
        System.out.println("Número de tandas: " + pms.getNumTandas());
        System.out.println("Repeticiones esperadas por tanda: " + pms.getNumRepeticionesEsperadas());
        
        
        for (int tandaNum = 1; tandaNum <= pms.getNumTandas(); tandaNum++) {
            final int tanda = tandaNum;
            List<RepPms> repeticionesTanda = todasLasRepeticiones.stream()
                .filter(rep -> rep.getNumTanda().equals(tanda))
                .collect(Collectors.toList());
            
            List<RepPms> repeticionesValidas = repeticionesTanda.stream()
                .filter(rep -> Boolean.TRUE.equals(rep.getValido()))
                .collect(Collectors.toList());
            
            System.out.println("Tanda " + tanda + ": " + repeticionesTanda.size() + " repeticiones totales, " + 
                             repeticionesValidas.size() + " válidas (necesita " + pms.getNumRepeticionesEsperadas() + ")");
            
            
            if (repeticionesValidas.size() >= pms.getNumRepeticionesEsperadas()) {
                
                EstadisticasTandaDTO estadisticas = calcularEstadisticasTanda(repeticionesValidas);
                BigDecimal umbralCV = pms.getEsSemillaBrozosa() ? 
                    new BigDecimal("6.0") : new BigDecimal("4.0");
                
                System.out.println("CV de tanda " + tanda + ": " + estadisticas.getCoeficienteVariacion() + 
                                 " (umbral: " + umbralCV + ")");
                
                
                if (estadisticas.getCoeficienteVariacion().compareTo(umbralCV) <= 0) {
                    System.out.println("Tanda " + tanda + " tiene CV válido. Repeticiones completas.");
                    return true;
                }
            }
        }
        
        
        if (totalRepeticiones >= 16) {
            System.out.println("Se alcanzó el límite de 16 repeticiones sin CV válido. Permitir finalización.");
            return true; 
        }
        
        System.out.println("No hay tandas con CV válido y no se alcanzó el límite. Retornando false.");
        return false;
    }

    /**
     * Validación completa para operaciones críticas de PMS (finalizar y marcar para repetir)
     * Verifica completitud de repeticiones y presencia de promedio con redondeo
     */
    private void validarPmsParaOperacionCritica(Pms pms) {
        
        if (!todasLasRepeticionesCompletas(pms)) {
            throw new RuntimeException("No se puede completar la operación hasta completar todas las repeticiones válidas");
        }
        
        if (pms.getPmsconRedon() == null) {
            throw new RuntimeException("Debe ingresar el promedio con redondeo (PMS con redondeo) antes de completar la operación");
        }
    }

    
    public PmsDTO finalizarAnalisis(Long id) {
        return analisisService.finalizarAnalisisGenerico(
            id, 
            pmsRepository, 
            this::mapearEntidadADTO,
            this::validarPmsParaOperacionCritica
        );
    }

    
    public PmsDTO aprobarAnalisis(Long id) {
        return analisisService.aprobarAnalisisGenerico(
            id,
            pmsRepository,
            this::mapearEntidadADTO,
            this::validarPmsParaOperacionCritica, 
            (idLote) -> pmsRepository.findByIdLote(idLote.intValue()) 
        );
    }

    
    public PmsDTO marcarParaRepetir(Long id) {
        return analisisService.marcarParaRepetirGenerico(
            id,
            pmsRepository,
            this::mapearEntidadADTO,
            this::validarPmsParaOperacionCritica 
        );
    }
}
