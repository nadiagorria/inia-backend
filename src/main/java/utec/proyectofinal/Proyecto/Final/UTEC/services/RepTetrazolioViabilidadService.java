package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepTetrazolioViabilidad;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepTetrazolioViabilidadRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepTetrazolioViabilidadRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepTetrazolioViabilidadDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Service
public class RepTetrazolioViabilidadService {

    @Autowired
    private RepTetrazolioViabilidadRepository repeticionRepository;

    @Autowired
    private TetrazolioRepository tetrazolioRepository;

    
    public RepTetrazolioViabilidadDTO crearRepeticion(Long tetrazolioId, RepTetrazolioViabilidadRequestDTO solicitud) {
        try {
            System.out.println("Creando repetición para tetrazolio ID: " + tetrazolioId);
            
            
            Optional<Tetrazolio> tetrazolioOpt = tetrazolioRepository.findById(tetrazolioId);
            if (tetrazolioOpt.isEmpty()) {
                throw new RuntimeException("Tetrazolio no encontrado con ID: " + tetrazolioId);
            }
            
            Tetrazolio tetrazolio = tetrazolioOpt.get();
            
            
            validarLimiteRepeticiones(tetrazolio);
            
            
            RepTetrazolioViabilidad repeticion = mapearSolicitudAEntidad(solicitud, tetrazolio);
            RepTetrazolioViabilidad repeticionGuardada = repeticionRepository.save(repeticion);
            
            
            if (tetrazolio.getEstado() == Estado.REGISTRADO) {
                tetrazolio.setEstado(Estado.EN_PROCESO);
                tetrazolioRepository.save(tetrazolio);
                System.out.println("Estado de Tetrazolio cambiado a EN_PROCESO al agregar primera repetición");
            }
            
            System.out.println("Repetición creada exitosamente con ID: " + repeticionGuardada.getRepTetrazolioViabID());
            return mapearEntidadADTO(repeticionGuardada);
        } catch (Exception e) {
            System.err.println("Error al crear repetición: " + e.getMessage());
            throw new RuntimeException("Error al crear la repetición: " + e.getMessage());
        }
    }

    
    public RepTetrazolioViabilidadDTO obtenerRepeticionPorId(Long id) {
        Optional<RepTetrazolioViabilidad> repeticion = repeticionRepository.findById(id);
        if (repeticion.isPresent()) {
            return mapearEntidadADTO(repeticion.get());
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    
    public RepTetrazolioViabilidadDTO actualizarRepeticion(Long id, RepTetrazolioViabilidadRequestDTO solicitud) {
        Optional<RepTetrazolioViabilidad> repeticionExistente = repeticionRepository.findById(id);
        
        if (repeticionExistente.isPresent()) {
            RepTetrazolioViabilidad repeticion = repeticionExistente.get();
            actualizarEntidadDesdeSolicitud(repeticion, solicitud);
            RepTetrazolioViabilidad repeticionActualizada = repeticionRepository.save(repeticion);
            return mapearEntidadADTO(repeticionActualizada);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    
    public void eliminarRepeticion(Long id) {
        Optional<RepTetrazolioViabilidad> repeticionExistente = repeticionRepository.findById(id);
        
        if (repeticionExistente.isPresent()) {
            repeticionRepository.deleteById(id);
            System.out.println("Repetición eliminada con ID: " + id);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    
    public List<RepTetrazolioViabilidadDTO> obtenerRepeticionesPorTetrazolio(Long tetrazolioId) {
        List<RepTetrazolioViabilidad> repeticiones = repeticionRepository.findByTetrazolioId(tetrazolioId);
        return repeticiones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public Long contarRepeticionesPorTetrazolio(Long tetrazolioId) {
        return repeticionRepository.countByTetrazolioId(tetrazolioId);
    }

    
    private RepTetrazolioViabilidad mapearSolicitudAEntidad(RepTetrazolioViabilidadRequestDTO solicitud, Tetrazolio tetrazolio) {
        RepTetrazolioViabilidad repeticion = new RepTetrazolioViabilidad();
        repeticion.setFecha(solicitud.getFecha());
        repeticion.setViablesNum(solicitud.getViablesNum());
        repeticion.setNoViablesNum(solicitud.getNoViablesNum());
        repeticion.setDuras(solicitud.getDuras());
        repeticion.setTetrazolio(tetrazolio);
        return repeticion;
    }

    
    private void actualizarEntidadDesdeSolicitud(RepTetrazolioViabilidad repeticion, RepTetrazolioViabilidadRequestDTO solicitud) {
        repeticion.setFecha(solicitud.getFecha());
        repeticion.setViablesNum(solicitud.getViablesNum());
        repeticion.setNoViablesNum(solicitud.getNoViablesNum());
        repeticion.setDuras(solicitud.getDuras());
        
    }

    
    private RepTetrazolioViabilidadDTO mapearEntidadADTO(RepTetrazolioViabilidad repeticion) {
        RepTetrazolioViabilidadDTO dto = new RepTetrazolioViabilidadDTO();
        dto.setRepTetrazolioViabID(repeticion.getRepTetrazolioViabID());
        dto.setFecha(repeticion.getFecha());
        dto.setViablesNum(repeticion.getViablesNum());
        dto.setNoViablesNum(repeticion.getNoViablesNum());
        dto.setDuras(repeticion.getDuras());
        return dto;
    }
    
    
    private void validarLimiteRepeticiones(Tetrazolio tetrazolio) {
        if (tetrazolio.getNumRepeticionesEsperadas() != null && tetrazolio.getNumRepeticionesEsperadas() > 0) {
            Long repeticionesExistentes = repeticionRepository.countByTetrazolioId(tetrazolio.getAnalisisID());
            
            if (repeticionesExistentes >= tetrazolio.getNumRepeticionesEsperadas()) {
                throw new RuntimeException(
                    String.format("No se pueden crear más repeticiones. Ya se han creado %d de %d repeticiones esperadas para este análisis.", 
                                repeticionesExistentes, tetrazolio.getNumRepeticionesEsperadas()));
            }
        }
    }
}