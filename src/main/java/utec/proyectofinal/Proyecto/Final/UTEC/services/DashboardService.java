package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.DashboardStatsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPendienteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPorAprobarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisPendienteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisPorAprobarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections.AnalisisPendienteProjection;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections.AnalisisPorAprobarProjection;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private LoteRepository loteRepository;
    
    @Autowired
    private AnalisisRepository analisisRepository;
    
    @Autowired
    private AnalisisPendienteRepository analisisPendienteRepository;
    
    @Autowired
    private AnalisisPorAprobarRepository analisisPorAprobarRepository;
    
    @Autowired
    private LoteService loteService;

    public DashboardStatsDTO obtenerEstadisticas() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        
        stats.setLotesActivos(loteRepository.countLotesActivos());
        
        
        stats.setAnalisisPendientes(loteService.contarAnalisisPendientes());
        
        
        stats.setCompletadosHoy(analisisRepository.countCompletadosEnFecha(LocalDate.now(), Estado.APROBADO));
        
        
        stats.setAnalisisPorAprobar(analisisRepository.countByEstado(Estado.PENDIENTE_APROBACION));
        
        return stats;
    }
    
    
    public Page<AnalisisPendienteDTO> listarAnalisisPendientesPaginados(Pageable pageable) {
        Page<AnalisisPendienteProjection> proyecciones = analisisPendienteRepository.findAllPaginado(pageable);
        
        return proyecciones.map(p -> new AnalisisPendienteDTO(
            p.getLoteID(),
            p.getNomLote(),
            p.getFicha(),
            p.getEspecieNombre(),
            p.getCultivarNombre(),
            TipoAnalisis.valueOf(p.getTipoAnalisis())
        ));
    }
    
    
    public Page<AnalisisPorAprobarDTO> listarAnalisisPorAprobarPaginados(Pageable pageable) {
        Page<AnalisisPorAprobarProjection> proyecciones = analisisPorAprobarRepository.findAllPaginado(pageable);
        
        return proyecciones.map(p -> {
            AnalisisPorAprobarDTO dto = new AnalisisPorAprobarDTO();
            dto.setAnalisisID(p.getAnalisisID());
            dto.setTipo(TipoAnalisis.valueOf(p.getTipoAnalisis()));
            dto.setLoteID(p.getLoteID());
            dto.setNomLote(p.getNomLote());
            dto.setFicha(p.getFicha());
            
            
            if (p.getFechaInicio() != null) {
                dto.setFechaInicio(LocalDateTime.parse(p.getFechaInicio(), 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if (p.getFechaFin() != null) {
                dto.setFechaFin(LocalDateTime.parse(p.getFechaFin(), 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            return dto;
        });
    }
}
