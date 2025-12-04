package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoCrudRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;

@Service
public class CatalogoService {

    @Autowired
    private CatalogoCrudRepository catalogoRepository;

    
    public List<CatalogoDTO> obtenerTodos() {
        return catalogoRepository.findByActivoTrue().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public List<CatalogoDTO> obtenerPorTipo(String tipo) {
        TipoCatalogo tipoCatalogo = TipoCatalogo.valueOf(tipo.toUpperCase());
        return catalogoRepository.findByTipoAndActivoTrue(tipoCatalogo).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public List<CatalogoDTO> obtenerPorTipo(String tipo, Boolean activo) {
        TipoCatalogo tipoCatalogo = TipoCatalogo.valueOf(tipo.toUpperCase());
        
        if (activo == null) {
            
            return catalogoRepository.findByTipo(tipoCatalogo).stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else if (activo) {
            
            return catalogoRepository.findByTipoAndActivoTrue(tipoCatalogo).stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else {
            
            return catalogoRepository.findByTipoAndActivoFalse(tipoCatalogo).stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        }
    }
    

    
    public CatalogoDTO obtenerPorId(Long id) {
        return catalogoRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElse(null);
    }

    
    public CatalogoDTO crear(CatalogoRequestDTO solicitud) {
        
        TipoCatalogo tipo = TipoCatalogo.valueOf(solicitud.getTipo().toUpperCase());
        Optional<Catalogo> existente = catalogoRepository.findByTipoAndValor(tipo, solicitud.getValor());
        
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe un catálogo con el mismo tipo y valor");
        }

        Catalogo catalogo = new Catalogo();
        catalogo.setTipo(tipo);
        catalogo.setValor(solicitud.getValor());
        catalogo.setActivo(true); 

        Catalogo guardado = catalogoRepository.save(catalogo);
        return mapearEntidadADTO(guardado);
    }

    
    public CatalogoDTO actualizar(Long id, CatalogoRequestDTO solicitud) {
        return catalogoRepository.findById(id)
                .map(catalogo -> {
                    
                    TipoCatalogo tipo = TipoCatalogo.valueOf(solicitud.getTipo().toUpperCase());
                    Optional<Catalogo> existente = catalogoRepository.findByTipoAndValor(tipo, solicitud.getValor());
                    
                    if (existente.isPresent() && !existente.get().getId().equals(id)) {
                        throw new RuntimeException("Ya existe un catálogo con el mismo tipo y valor");
                    }

                    catalogo.setTipo(tipo);
                    catalogo.setValor(solicitud.getValor());

                    Catalogo actualizado = catalogoRepository.save(catalogo);
                    return mapearEntidadADTO(actualizado);
                })
                .orElse(null);
    }

    
    public void eliminar(Long id) {
        catalogoRepository.findById(id)
                .ifPresent(catalogo -> {
                    catalogo.setActivo(false);
                    catalogoRepository.save(catalogo);
                });
    }

    
    public CatalogoDTO reactivar(Long id) {
        return catalogoRepository.findById(id)
                .map(catalogo -> {
                    catalogo.setActivo(true);
                    Catalogo reactivado = catalogoRepository.save(catalogo);
                    return mapearEntidadADTO(reactivado);
                })
                .orElse(null);
    }

    
    public void eliminarFisicamente(Long id) {
        catalogoRepository.deleteById(id);
    }

    
    private CatalogoDTO mapearEntidadADTO(Catalogo catalogo) {
        CatalogoDTO dto = new CatalogoDTO();
        dto.setId(catalogo.getId());
        dto.setTipo(catalogo.getTipo().name());
        dto.setValor(catalogo.getValor());
        dto.setActivo(catalogo.getActivo());
        return dto;
    }

    
    public Catalogo obtenerEntidadPorId(Long id) {
        return catalogoRepository.findById(id).orElse(null);
    }

    
    public Page<CatalogoDTO> obtenerCatalogosPaginados(Pageable pageable) {
        Page<Catalogo> catalogoPage = catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable);
        return catalogoPage.map(this::mapearEntidadADTO);
    }

    
    public Page<CatalogoDTO> obtenerCatalogosPaginadosConFiltro(Pageable pageable, String filtroActivo) {
        Page<Catalogo> catalogoPage;
        
        if ("activos".equalsIgnoreCase(filtroActivo)) {
            catalogoPage = catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable);
        } else if ("inactivos".equalsIgnoreCase(filtroActivo)) {
            catalogoPage = catalogoRepository.findByActivoFalseOrderByTipoAscValorAsc(pageable);
        } else {
            
            catalogoPage = catalogoRepository.findAllByOrderByTipoAscValorAsc(pageable);
        }
        
        return catalogoPage.map(this::mapearEntidadADTO);
    }

    
    public Page<CatalogoDTO> obtenerCatalogosPaginadosConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            String tipo) {
        
        Page<Catalogo> catalogoPage;
        
        
        if (tipo != null && !tipo.trim().isEmpty()) {
            try {
                TipoCatalogo tipoCatalogo = TipoCatalogo.valueOf(tipo.toUpperCase());
                
                
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    List<Catalogo> catalogos;
                    if (activo == null) {
                        catalogos = catalogoRepository.findByTipo(tipoCatalogo);
                    } else if (activo) {
                        catalogos = catalogoRepository.findByTipoAndActivoTrue(tipoCatalogo);
                    } else {
                        catalogos = catalogoRepository.findByTipoAndActivoFalse(tipoCatalogo);
                    }
                    
                    
                    catalogos = catalogos.stream()
                        .filter(c -> c.getValor() != null && c.getValor().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    
                    
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), catalogos.size());
                    List<Catalogo> pageContent = catalogos.subList(start, end);
                    catalogoPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, catalogos.size());
                } else {
                    
                    if (activo == null) {
                        catalogoPage = catalogoRepository.findByTipoOrderByValorAsc(tipoCatalogo, pageable);
                    } else if (activo) {
                        catalogoPage = catalogoRepository.findByTipoAndActivoTrueOrderByValorAsc(tipoCatalogo, pageable);
                    } else {
                        catalogoPage = catalogoRepository.findByTipoAndActivoFalseOrderByValorAsc(tipoCatalogo, pageable);
                    }
                }
            } catch (IllegalArgumentException e) {
                
                catalogoPage = Page.empty(pageable);
            }
        } else {
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                List<Catalogo> catalogos;
                if (activo == null) {
                    catalogos = catalogoRepository.findAll();
                } else if (activo) {
                    catalogos = catalogoRepository.findByActivoTrue();
                } else {
                    catalogos = catalogoRepository.findAll().stream()
                        .filter(c -> !c.getActivo())
                        .collect(Collectors.toList());
                }
                
                
                catalogos = catalogos.stream()
                    .filter(c -> c.getValor() != null && c.getValor().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
                
                
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), catalogos.size());
                List<Catalogo> pageContent = catalogos.subList(start, end);
                catalogoPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, catalogos.size());
            } else {
                
                if (activo == null) {
                    catalogoPage = catalogoRepository.findAllByOrderByTipoAscValorAsc(pageable);
                } else if (activo) {
                    catalogoPage = catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable);
                } else {
                    catalogoPage = catalogoRepository.findByActivoFalseOrderByTipoAscValorAsc(pageable);
                }
            }
        }
        
        return catalogoPage.map(this::mapearEntidadADTO);
    }
}