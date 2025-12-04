package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ValoresGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ValoresGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

@Service
public class ValoresGermService {

    @Autowired
    private ValoresGermRepository valoresGermRepository;
    
    @Autowired
    private AnalisisService analisisService;

    
    public ValoresGermDTO obtenerValoresPorId(Long id) {
        Optional<ValoresGerm> valores = valoresGermRepository.findById(id);
        if (valores.isPresent()) {
            return mapearEntidadADTO(valores.get());
        } else {
            throw new RuntimeException("Valores de germinación no encontrados con ID: " + id);
        }
    }

    
    public ValoresGermDTO actualizarValores(Long id, ValoresGermRequestDTO solicitud) {
        Optional<ValoresGerm> valoresExistentes = valoresGermRepository.findById(id);
        
        if (valoresExistentes.isPresent()) {
            ValoresGerm valores = valoresExistentes.get();
            
            
            analisisService.manejarEdicionAnalisisFinalizado(valores.getTablaGerm().getGerminacion());
            
            actualizarEntidadDesdeSolicitud(valores, solicitud);
            ValoresGerm valoresActualizados = valoresGermRepository.save(valores);
            System.out.println("Valores de germinación actualizados para instituto: " + valores.getInstituto());
            return mapearEntidadADTO(valoresActualizados);
        } else {
            throw new RuntimeException("Valores de germinación no encontrados con ID: " + id);
        }
    }

    
    public ValoresGermDTO obtenerValoresPorTablaEInstituto(Long tablaId, Instituto instituto) {
        Optional<ValoresGerm> valores = valoresGermRepository.findByTablaGermIdAndInstituto(tablaId, instituto);
        if (valores.isPresent()) {
            return mapearEntidadADTO(valores.get());
        } else {
            throw new RuntimeException("Valores no encontrados para tabla " + tablaId + " e instituto " + instituto);
        }
    }

    
    public List<ValoresGermDTO> obtenerValoresPorTabla(Long tablaId) {
        List<ValoresGerm> valores = valoresGermRepository.findByTablaGermId(tablaId);
        return valores.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public ValoresGermDTO obtenerValoresIniaPorTabla(Long tablaId) {
        return obtenerValoresPorTablaEInstituto(tablaId, Instituto.INIA);
    }

    
    public ValoresGermDTO obtenerValoresInasePorTabla(Long tablaId) {
        return obtenerValoresPorTablaEInstituto(tablaId, Instituto.INASE);
    }

    
    public void eliminarValores(Long id) {
        Optional<ValoresGerm> valoresExistentes = valoresGermRepository.findById(id);
        
        if (valoresExistentes.isPresent()) {
            ValoresGerm valores = valoresExistentes.get();
            valoresGermRepository.deleteById(id);
            System.out.println("Valores de germinación eliminados para instituto: " + valores.getInstituto());
        } else {
            throw new RuntimeException("Valores de germinación no encontrados con ID: " + id);
        }
    }

    
    private void actualizarEntidadDesdeSolicitud(ValoresGerm valores, ValoresGermRequestDTO solicitud) {
        
        validarSumaValores(solicitud, valores.getInstituto());
        
        valores.setNormales(solicitud.getNormales());
        valores.setAnormales(solicitud.getAnormales());
        valores.setDuras(solicitud.getDuras());
        valores.setFrescas(solicitud.getFrescas());
        valores.setMuertas(solicitud.getMuertas());
        valores.setGerminacion(solicitud.getGerminacion());
        
    }
    
    
    private void validarSumaValores(ValoresGermRequestDTO solicitud, Instituto instituto) {
        BigDecimal suma = BigDecimal.ZERO;
        
        if (solicitud.getNormales() != null) {
            suma = suma.add(solicitud.getNormales());
        }
        if (solicitud.getAnormales() != null) {
            suma = suma.add(solicitud.getAnormales());
        }
        if (solicitud.getDuras() != null) {
            suma = suma.add(solicitud.getDuras());
        }
        if (solicitud.getFrescas() != null) {
            suma = suma.add(solicitud.getFrescas());
        }
        if (solicitud.getMuertas() != null) {
            suma = suma.add(solicitud.getMuertas());
        }
        
        if (suma.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("La suma de los valores (normales + anormales + duras + frescas + muertas) para " + 
                                     instituto + " no puede superar 100. Suma actual: " + suma);
        }
    }

    
    private ValoresGermDTO mapearEntidadADTO(ValoresGerm valores) {
        ValoresGermDTO dto = new ValoresGermDTO();
        dto.setValoresGermID(valores.getValoresGermID());
        dto.setNormales(valores.getNormales());
        dto.setAnormales(valores.getAnormales());
        dto.setDuras(valores.getDuras());
        dto.setFrescas(valores.getFrescas());
        dto.setMuertas(valores.getMuertas());
        dto.setGerminacion(valores.getGerminacion());
        dto.setInstituto(valores.getInstituto());
        
        
        if (valores.getTablaGerm() != null) {
            dto.setTablaGermId(valores.getTablaGerm().getTablaGermID());
        }
        
        return dto;
    }
}