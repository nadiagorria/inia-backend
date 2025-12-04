package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepPmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepPmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepPmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Service
public class RepPmsService {

    @Autowired
    private RepPmsRepository repPmsRepository;

    @Autowired
    private PmsRepository pmsRepository;

    @Autowired
    private PmsService pmsService;

    @Autowired
    private AnalisisService analisisService;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;

    
    public RepPmsDTO crearRepeticion(Long pmsId, RepPmsRequestDTO solicitud) {
        Optional<Pms> pmsOpt = pmsRepository.findById(pmsId);
        if (pmsOpt.isEmpty()) {
            throw new RuntimeException("Pms no encontrado con ID: " + pmsId);
        }
        Pms pms = pmsOpt.get();

        
        if (pms.getEstado() != Estado.REGISTRADO && pms.getEstado() != Estado.EN_PROCESO) {
            throw new RuntimeException("No se pueden agregar repeticiones a un PMS que no esté en estado REGISTRADO o EN_PROCESO");
        }

        
        long totalRepeticiones = repPmsRepository.countByPmsId(pmsId);
        if (totalRepeticiones >= 16) {
            throw new RuntimeException("No se pueden crear más de 16 repeticiones para un análisis PMS");
        }

        
        Integer tandaActual = determinarTandaActual(pms);

        
        long repeticionesValidasTandaActual = repPmsRepository.findByPmsId(pmsId).stream()
            .filter(rep -> rep.getNumTanda().equals(tandaActual) && Boolean.TRUE.equals(rep.getValido()))
            .count();

        
        long repeticionesTotalesTandaActual = repPmsRepository.findByPmsId(pmsId).stream()
            .filter(rep -> rep.getNumTanda().equals(tandaActual))
            .count();

        
        

        RepPms repeticion = mapearSolicitudAEntidad(solicitud, pms, tandaActual);
        RepPms guardada = repPmsRepository.save(repeticion);

        
        if (pms.getEstado() == Estado.REGISTRADO) {
            pms.setEstado(Estado.EN_PROCESO);
            
            pmsRepository.save(pms);
        }

        
        if (repeticionesTotalesTandaActual + 1 >= pms.getNumRepeticionesEsperadas()) {
            System.out.println(" Tanda completa! Validando todas las repeticiones del PMS ID: " + pmsId);
            System.out.println("  Repeticiones totales: " + (repeticionesTotalesTandaActual + 1) + " >= Esperadas: " + pms.getNumRepeticionesEsperadas());
            
            
            pmsService.validarTodasLasRepeticiones(pmsId);
            
            
            RepPms repeticionActualizada = repPmsRepository.findById(guardada.getRepPMSID())
                .orElseThrow(() -> new RuntimeException("Error al recargar repetición creada"));
            
            return mapearEntidadADTO(repeticionActualizada);
        }

        
        analisisHistorialService.registrarModificacion(pms);

        return mapearEntidadADTO(guardada);
    }

    
    public RepPmsDTO obtenerPorId(Long id) {
        Optional<RepPms> repeticion = repPmsRepository.findById(id);
        if (repeticion.isPresent()) {
            return mapearEntidadADTO(repeticion.get());
        } else {
            throw new RuntimeException("Repetición PMS no encontrada con ID: " + id);
        }
    }

    
    public RepPmsDTO actualizarRepeticion(Long id, RepPmsRequestDTO solicitud) {
        Optional<RepPms> existente = repPmsRepository.findById(id);

        if (existente.isPresent()) {
            RepPms rep = existente.get();
            
            
            analisisService.manejarEdicionAnalisisFinalizado(rep.getPms());
            
            actualizarEntidadDesdeSolicitud(rep, solicitud);
            RepPms actualizado = repPmsRepository.save(rep);
            
            
            Pms pms = rep.getPms();
            
            System.out.println("=== Actualización de repetición ===");
            System.out.println("PMS ID: " + pms.getAnalisisID());
            System.out.println("Rep ID: " + id);
            
            
            long repeticionesTotales = repPmsRepository.countByPmsId(pms.getAnalisisID());
            
            System.out.println("Repeticiones totales del PMS: " + repeticionesTotales);
            System.out.println("Repeticiones esperadas: " + pms.getNumRepeticionesEsperadas());
            
            
            if (repeticionesTotales >= pms.getNumRepeticionesEsperadas()) {
                System.out.println("Validando todas las repeticiones del PMS...");
                pmsService.validarTodasLasRepeticiones(pms.getAnalisisID());
            } else {
                System.out.println("Repeticiones insuficientes, solo actualizando estadísticas generales...");
                pmsService.actualizarEstadisticasPms(pms.getAnalisisID());
            }
            
            
            RepPms repeticionActualizada = repPmsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error al recargar repetición actualizada"));
            
            
            analisisHistorialService.registrarModificacion(pms);
            
            return mapearEntidadADTO(repeticionActualizada);
        } else {
            throw new RuntimeException("Repetición PMS no encontrada con ID: " + id);
        }
    }

    
    public void eliminarRepeticion(Long id) {
        Optional<RepPms> existente = repPmsRepository.findById(id);

        if (existente.isPresent()) {
            RepPms rep = existente.get();
            Pms pms = rep.getPms();
            Integer numTanda = rep.getNumTanda();
            
            System.out.println("=== Eliminación de repetición ===");
            System.out.println("PMS ID: " + pms.getAnalisisID());
            System.out.println("Repetición a eliminar ID: " + id);
            
            
            repPmsRepository.deleteById(id);
            
            
            long repeticionesTotales = repPmsRepository.countByPmsId(pms.getAnalisisID());
            
            System.out.println("Repeticiones restantes en el PMS: " + repeticionesTotales);
            System.out.println("Repeticiones esperadas: " + pms.getNumRepeticionesEsperadas());
            
            
            if (repeticionesTotales >= pms.getNumRepeticionesEsperadas()) {
                System.out.println("Validando todas las repeticiones del PMS...");
                pmsService.validarTodasLasRepeticiones(pms.getAnalisisID());
            } else {
                System.out.println("Repeticiones insuficientes, reseteando validaciones...");
                
                List<RepPms> repeticionesRestantes = repPmsRepository.findByPmsId(pms.getAnalisisID());
                for (RepPms r : repeticionesRestantes) {
                    r.setValido(null);
                }
                repPmsRepository.saveAll(repeticionesRestantes);
                pmsService.actualizarEstadisticasPms(pms.getAnalisisID());
            }
            
            
            analisisHistorialService.registrarModificacion(pms);
        } else {
            throw new RuntimeException("Repetición PMS no encontrada con ID: " + id);
        }
    }

    
    public List<RepPmsDTO> obtenerPorPms(Long pmsId) {
        List<RepPms> repeticiones = repPmsRepository.findByPmsId(pmsId);
        return repeticiones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public Long contarPorPms(Long pmsId) {
        return repPmsRepository.countByPmsId(pmsId);
    }

    
    
    

    private RepPms mapearSolicitudAEntidad(RepPmsRequestDTO solicitud, Pms pms, Integer numTanda) {
        RepPms rep = new RepPms();
        rep.setNumRep(solicitud.getNumRep());
        rep.setPeso(solicitud.getPeso());
        rep.setNumTanda(numTanda);
        rep.setValido(null); 
        rep.setPms(pms);
        return rep;
    }

    private void actualizarEntidadDesdeSolicitud(RepPms rep, RepPmsRequestDTO solicitud) {
        rep.setNumRep(solicitud.getNumRep());
        rep.setPeso(solicitud.getPeso());
    }

    private RepPmsDTO mapearEntidadADTO(RepPms rep) {
        RepPmsDTO dto = new RepPmsDTO();
        dto.setRepPMSID(rep.getRepPMSID());
        dto.setNumRep(rep.getNumRep());
        dto.setPeso(rep.getPeso());
        dto.setNumTanda(rep.getNumTanda());
        dto.setValido(rep.getValido());
        return dto;
    }

    
    private Integer determinarTandaActual(Pms pms) {
        
        List<RepPms> todasLasRepeticiones = repPmsRepository.findByPmsId(pms.getAnalisisID());
        
        if (todasLasRepeticiones.isEmpty()) {
            return 1; 
        }

        
        for (int tandaNum = 1; tandaNum <= pms.getNumTandas(); tandaNum++) {
            final int tanda = tandaNum;
            long repeticionesValidas = todasLasRepeticiones.stream()
                .filter(rep -> rep.getNumTanda().equals(tanda) && Boolean.TRUE.equals(rep.getValido()))
                .count();
            
            
            if (repeticionesValidas < pms.getNumRepeticionesEsperadas()) {
                return tanda; 
            }
        }

        
        long totalRepeticiones = todasLasRepeticiones.size();
        if (totalRepeticiones >= 16) {
            throw new RuntimeException("Se alcanzó el límite máximo de 16 repeticiones");
        }

        
        List<RepPms> repeticionesValidas = todasLasRepeticiones.stream()
            .filter(rep -> Boolean.TRUE.equals(rep.getValido()))
            .collect(Collectors.toList());

        if (!repeticionesValidas.isEmpty()) {
            
            double promedio = repeticionesValidas.stream()
                .mapToDouble(rep -> rep.getPeso().doubleValue())
                .average()
                .orElse(0.0);

            double suma = repeticionesValidas.stream()
                .mapToDouble(rep -> Math.pow(rep.getPeso().doubleValue() - promedio, 2))
                .sum();

            double desviacion = Math.sqrt(suma / repeticionesValidas.size());
            double cv = promedio != 0 ? (desviacion / promedio) * 100 : 0;

            double umbralCV = pms.getEsSemillaBrozosa() ? 6.0 : 4.0;
            
            if (cv <= umbralCV) {
                throw new RuntimeException("El CV global (" + String.format("%.2f", cv) + 
                    ") ya es aceptable (≤ " + umbralCV + "). No se necesitan más repeticiones.");
            }
        }

        
        int nuevaTanda = pms.getNumTandas() + 1;
        pms.setNumTandas(nuevaTanda);
        pmsRepository.save(pms);
        
        System.out.println("=== Incrementando tandas ===");
        System.out.println("CV no aceptable. Se incrementa el número de tandas a: " + nuevaTanda);
        
        return nuevaTanda;
    }
}
