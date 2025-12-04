package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.AnalisisHistorial;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisHistorialRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO;

@Service
public class AnalisisHistorialService {

    @Autowired
    private AnalisisHistorialRepository analisisHistorialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Registrar automáticamente cuando se crea un análisis
     */
    @Transactional
    public void registrarCreacion(Analisis analisis) {
        registrarAccion(analisis, "CREACION");
    }

    /**
     * Registrar automáticamente cuando se modifica un análisis
     */
    @Transactional
    public void registrarModificacion(Analisis analisis) {
        registrarAccion(analisis, "MODIFICACION");
    }

    /**
     * Método privado para registrar una acción en el historial
     */
    private void registrarAccion(Analisis analisis, String accion) {
        try {
            
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByNombreIgnoreCase(username)
                .orElse(null);

            if (usuario != null) {
                AnalisisHistorial historial = new AnalisisHistorial();
                historial.setAnalisis(analisis);
                historial.setUsuario(usuario);
                historial.setFechaHora(LocalDateTime.now());
                
                analisisHistorialRepository.save(historial);
            }
        } catch (Exception e) {
            
            System.err.println("Error registrando historial de análisis: " + e.getMessage());
        }
    }

    /**
     * Obtener historial de un análisis específico
     */
    public List<AnalisisHistorialDTO> obtenerHistorialAnalisis(Long analisisId) {
        List<AnalisisHistorial> historial = analisisHistorialRepository
            .findByAnalisisIdOrderByFechaHoraDesc(analisisId);
        
        return historial.stream()
            .map(this::mapearEntidadADTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtener historial de análisis por usuario
     */
    public List<AnalisisHistorialDTO> obtenerHistorialUsuario(Integer usuarioId) {
        List<AnalisisHistorial> historial = analisisHistorialRepository
            .findByUsuarioIdOrderByFechaHoraDesc(usuarioId);
        
        return historial.stream()
            .map(this::mapearEntidadADTO)
            .collect(Collectors.toList());
    }

    /**
     * Mapear entidad a DTO
     */
    private AnalisisHistorialDTO mapearEntidadADTO(AnalisisHistorial historial) {
        AnalisisHistorialDTO dto = new AnalisisHistorialDTO();
        dto.setId(historial.getId());
        dto.setUsuario(historial.getUsuario().getNombres() + " " + historial.getUsuario().getApellidos());
        dto.setFechaHora(historial.getFechaHora());
        
        
        List<AnalisisHistorial> todosRegistros = analisisHistorialRepository
            .findByAnalisisIdOrderByFechaHoraDesc(historial.getAnalisis().getAnalisisID());
        
        boolean esCreacion = todosRegistros.size() > 0 && 
            todosRegistros.get(todosRegistros.size() - 1).getId().equals(historial.getId());
        
        dto.setAccion(esCreacion ? "CREACION" : "MODIFICACION");
        
        return dto;
    }
}