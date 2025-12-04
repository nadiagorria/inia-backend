package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CultivarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CultivarRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CultivarDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CultivarService {

    @Autowired
    private CultivarRepository cultivarRepository;

    @Autowired
    private EspecieService especieService;

    
    public List<CultivarDTO> obtenerTodos() {
        return cultivarRepository.findByActivoTrue().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public List<CultivarDTO> obtenerInactivos() {
        return cultivarRepository.findByActivoFalse().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public List<CultivarDTO> obtenerTodos(Boolean activo) {
        if (activo == null) {
            
            return cultivarRepository.findAll().stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else if (activo) {
            return obtenerTodos();
        } else {
            return obtenerInactivos();
        }
    }

    
    public List<CultivarDTO> obtenerPorEspecie(Long especieID) {
        return cultivarRepository.findByEspecieEspecieIDAndActivoTrue(especieID).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public List<CultivarDTO> buscarPorNombre(String nombre) {
        return cultivarRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public CultivarDTO obtenerPorId(Long id) {
        return cultivarRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElse(null);
    }

    
    public CultivarDTO crear(CultivarRequestDTO solicitud) {
        Especie especie = especieService.obtenerEntidadPorId(solicitud.getEspecieID());
        if (especie == null) {
            throw new RuntimeException("Especie no encontrada con ID: " + solicitud.getEspecieID());
        }

        Cultivar cultivar = new Cultivar();
        cultivar.setEspecie(especie);
        cultivar.setNombre(solicitud.getNombre());
        cultivar.setActivo(true);

        Cultivar guardado = cultivarRepository.save(cultivar);
        return mapearEntidadADTO(guardado);
    }

    
    public CultivarDTO actualizar(Long id, CultivarRequestDTO solicitud) {
        return cultivarRepository.findById(id)
                .map(cultivar -> {
                    if (solicitud.getEspecieID() != null) {
                        Especie especie = especieService.obtenerEntidadPorId(solicitud.getEspecieID());
                        if (especie == null) {
                            throw new RuntimeException("Especie no encontrada con ID: " + solicitud.getEspecieID());
                        }
                        cultivar.setEspecie(especie);
                    }
                    
                    cultivar.setNombre(solicitud.getNombre());

                    Cultivar actualizado = cultivarRepository.save(cultivar);
                    return mapearEntidadADTO(actualizado);
                })
                .orElse(null);
    }

    
    public void eliminar(Long id) {
        cultivarRepository.findById(id)
                .ifPresent(cultivar -> {
                    cultivar.setActivo(false);
                    cultivarRepository.save(cultivar);
                });
    }

    
    public CultivarDTO reactivar(Long id) {
        return cultivarRepository.findById(id)
                .map(cultivar -> {
                    if (cultivar.getActivo() != null && cultivar.getActivo()) {
                        throw new RuntimeException("El cultivar ya está activo");
                    }
                    cultivar.setActivo(true);
                    Cultivar reactivado = cultivarRepository.save(cultivar);
                    return mapearEntidadADTO(reactivado);
                })
                .orElse(null);
    }

    
    private CultivarDTO mapearEntidadADTO(Cultivar cultivar) {
        CultivarDTO dto = new CultivarDTO();
        dto.setCultivarID(cultivar.getCultivarID());
        dto.setNombre(cultivar.getNombre());
        dto.setActivo(cultivar.getActivo());
        
        if (cultivar.getEspecie() != null) {
            dto.setEspecieID(cultivar.getEspecie().getEspecieID());
            dto.setEspecieNombre(cultivar.getEspecie().getNombreComun());
        }
        
        return dto;
    }

    
    public Cultivar obtenerEntidadPorId(Long id) {
        return cultivarRepository.findById(id).orElse(null);
    }

    
    public Page<CultivarDTO> obtenerCultivaresPaginadosConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo) {
        
        Page<Cultivar> cultivarPage;
        
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<Cultivar> cultivares;
            if (activo == null) {
                
                cultivares = cultivarRepository.findAll().stream()
                    .filter(c -> c.getNombre() != null && 
                                c.getNombre().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
            } else if (activo) {
                
                cultivares = cultivarRepository.findByNombreContainingIgnoreCaseAndActivoTrue(searchTerm);
            } else {
                
                cultivares = cultivarRepository.findByActivoFalse().stream()
                    .filter(c -> c.getNombre() != null && 
                                c.getNombre().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), cultivares.size());
            List<Cultivar> pageContent = cultivares.subList(start, end);
            cultivarPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, cultivares.size());
        } else {
            
            if (activo == null) {
                cultivarPage = cultivarRepository.findAllByOrderByNombreAsc(pageable);
            } else if (activo) {
                cultivarPage = cultivarRepository.findByActivoTrueOrderByNombreAsc(pageable);
            } else {
                cultivarPage = cultivarRepository.findByActivoFalseOrderByNombreAsc(pageable);
            }
        }
        
        return cultivarPage.map(this::mapearEntidadADTO);
    }
}