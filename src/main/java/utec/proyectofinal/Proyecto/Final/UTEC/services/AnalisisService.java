package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;


@Service
public class AnalisisService {

    @Autowired
    private AnalisisHistorialService analisisHistorialService;

    @Autowired
    private NotificacionService notificacionService;

    
    public void establecerFechaInicio(Analisis analisis) {
        analisis.setFechaInicio(LocalDateTime.now());
    }

    
    public Analisis finalizarAnalisis(Analisis analisis) {
        
        if (!analisis.getActivo()) {
            throw new RuntimeException("No se puede finalizar un análisis inactivo");
        }
        
        
        if (analisis.getEstado() == Estado.APROBADO || analisis.getEstado() == Estado.A_REPETIR) {
            throw new RuntimeException("El análisis ya está finalizado o marcado para repetir");
        }

        boolean esAnalista = esAnalista();
        
        if (esAnalista) {
            // Analista: enviar a pendiente de aprobación
            analisis.setEstado(Estado.PENDIENTE_APROBACION);
        } else {
            // Admin: aprobar directamente
            analisis.setEstado(Estado.APROBADO);
        }
        
        
        analisis.setFechaFin(LocalDateTime.now());
        
        
        analisisHistorialService.registrarModificacion(analisis);
        
        // Crear notificación automática SOLO si es analista
        // Si es admin, ya aprobó directamente y no necesita notificación
        if (esAnalista) {
            try {
                notificacionService.notificarAnalisisFinalizado(analisis.getAnalisisID());
            } catch (Exception e) {
                
                System.err.println("Error creating notification for analysis finalization: " + e.getMessage());
            }
        }
        
        return analisis;
    }

    
    public Analisis aprobarAnalisis(Analisis analisis) {
        
        if (!analisis.getActivo()) {
            throw new RuntimeException("No se puede aprobar un análisis inactivo");
        }
        
        
        if (analisis.getEstado() != Estado.PENDIENTE_APROBACION && analisis.getEstado() != Estado.A_REPETIR) {
            throw new RuntimeException("El análisis debe estar en estado PENDIENTE_APROBACION o A_REPETIR para ser aprobado");
        }
        
        analisis.setEstado(Estado.APROBADO);
        
        
        if (analisis.getFechaFin() == null) {
            analisis.setFechaFin(LocalDateTime.now());
        }
        
        
        analisisHistorialService.registrarModificacion(analisis);
        
        // Crear notificación automática para aprobación de análisis
        try {
            notificacionService.notificarAnalisisAprobado(analisis.getAnalisisID());
        } catch (Exception e) {
            
            System.err.println("Error creating notification for analysis approval: " + e.getMessage());
        }
        
        return analisis;
    }
    
    
    public Analisis marcarParaRepetir(Analisis analisis) {
        
        if (!analisis.getActivo()) {
            throw new RuntimeException("No se puede marcar para repetir un análisis inactivo");
        }
        
        
        // (El validador específico se encarga de verificar requisitos por tipo de análisis)
        
        analisis.setEstado(Estado.A_REPETIR);
        
        
        analisisHistorialService.registrarModificacion(analisis);
        
        
        try {
            notificacionService.notificarAnalisisRepetir(analisis.getAnalisisID());
        } catch (Exception e) {
            
            System.err.println("Error creating notification for analysis rejection: " + e.getMessage());
        }
        
        return analisis;
    }

    
    public boolean esAnalista() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANALISTA"));
        }
        return false;
    }

    
    public void manejarEdicionAnalisisFinalizado(Analisis analisis) {
        
        if (analisis.getEstado() == Estado.APROBADO) {
            if (esAnalista()) {
                
                analisis.setEstado(Estado.PENDIENTE_APROBACION);
                
                
                analisisHistorialService.registrarModificacion(analisis);
                
                
                try {
                    notificacionService.notificarAnalisisPendienteAprobacion(analisis.getAnalisisID());
                } catch (Exception e) {
                    System.err.println("Error creating notification for analysis pending approval: " + e.getMessage());
                }
            }
            
        }
    }

    
    public <T extends Analisis, D> D finalizarAnalisisGenerico(
            Long id, 
            JpaRepository<T, Long> repository,
            Function<T, D> mapper,
            Consumer<T> validator) {
        
        T analisis = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        
        if (validator != null) {
            validator.accept(analisis);
        }
        
        
        finalizarAnalisis(analisis);
        
        
        T analisisActualizado = repository.save(analisis);
        
        return mapper.apply(analisisActualizado);
    }

    
    public <T extends Analisis, D> D aprobarAnalisisGenerico(
            Long id,
            JpaRepository<T, Long> repository,
            Function<T, D> mapper,
            Consumer<T> validator,
            Function<Long, java.util.List<T>> buscarPorLote) {
        
        T analisis = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        

        
        if (analisis.getEstado() == Estado.A_REPETIR && analisis.getLote() != null && buscarPorLote != null) {
            System.out.println("   Análisis está en A_REPETIR, validando si existen otros análisis válidos...");
            
            
            java.util.List<T> analisisDelMismoLote = buscarPorLote.apply(analisis.getLote().getLoteID());
            
            System.out.println("  - Total análisis del mismo tipo para este lote: " + analisisDelMismoLote.size());
            
            
            analisisDelMismoLote.forEach(a -> {
                System.out.println("    • ID: " + a.getAnalisisID() + 
                                 ", Estado: " + a.getEstado() + 
                                 ", Activo: " + a.getActivo() +
                                 (a.getAnalisisID().equals(analisis.getAnalisisID()) ? " (ACTUAL)" : ""));
            });
            
            
            boolean existeAnalisisValido = analisisDelMismoLote.stream()
                .filter(a -> !a.getAnalisisID().equals(analisis.getAnalisisID())) // Excluir el análisis actual
                .filter(a -> a.getActivo()) 
                .filter(a -> a.getEstado() != null) 
                .anyMatch(a -> a.getEstado() != Estado.A_REPETIR); 
            
            System.out.println("  - ¿Existe otro análisis válido (activo, con estado y no A_REPETIR)? " + existeAnalisisValido);
            
            if (existeAnalisisValido) {
                System.out.println("   ERROR: Ya existe un análisis válido, cancelando aprobación");
                throw new RuntimeException("Ya existe un análisis válido de este tipo para el lote " + 
                    analisis.getLote().getFicha() + ". No se puede aprobar este análisis marcado para repetir.");
            }
            
            System.out.println("   No existe otro análisis válido, procediendo con la aprobación...");
        }
        
        // Ejecutar validación específica si existe
        if (validator != null) {
            validator.accept(analisis);
        }
        
        
        aprobarAnalisis(analisis);
        
        
        T analisisActualizado = repository.save(analisis);
        
        System.out.println("   Análisis aprobado exitosamente, nuevo estado: " + analisisActualizado.getEstado());
        
        return mapper.apply(analisisActualizado);
    }

    
    public <T extends Analisis, D> D marcarParaRepetirGenerico(
            Long id,
            JpaRepository<T, Long> repository,
            Function<T, D> mapper,
            Consumer<T> validator) {
        
        T analisis = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        
        if (validator != null) {
            validator.accept(analisis);
        }
        
        
        marcarParaRepetir(analisis);
        
        
        T analisisActualizado = repository.save(analisis);
        return mapper.apply(analisisActualizado);
    }

    
    public <T extends Analisis> void desactivarAnalisis(
            Long id,
            JpaRepository<T, Long> repository) {
        
        T analisis = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        analisis.setActivo(false);
        repository.save(analisis);
    }

    
    public <T extends Analisis, D> D reactivarAnalisis(
            Long id,
            JpaRepository<T, Long> repository,
            Function<T, D> mapper) {
        
        T analisis = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        if (analisis.getActivo()) {
            throw new RuntimeException("El análisis ya está activo");
        }
        
        analisis.setActivo(true);
        T analisisReactivado = repository.save(analisis);
        return mapper.apply(analisisReactivado);
    }
}