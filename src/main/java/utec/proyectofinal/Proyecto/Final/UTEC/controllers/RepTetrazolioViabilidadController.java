package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepTetrazolioViabilidadRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepTetrazolioViabilidadDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepTetrazolioViabilidadService;


@RestController
@RequestMapping("/api/tetrazolios/{tetrazolioId}/repeticiones")
@Tag(name = "Repeticiones Tetrazolio Viabilidad", description = "API para gestión de repeticiones de análisis de tetrazolio viabilidad")
@SecurityRequirement(name = "bearerAuth")
public class RepTetrazolioViabilidadController {

    @Autowired
    private RepTetrazolioViabilidadService repeticionService;

    
    @Operation(summary = "Crear repetición de tetrazolio viabilidad", 
              description = "Crea una nueva repetición para un análisis de tetrazolio viabilidad específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PostMapping
    public ResponseEntity<RepTetrazolioViabilidadDTO> crearRepeticion(
            @PathVariable Long tetrazolioId,
            @RequestBody RepTetrazolioViabilidadRequestDTO solicitud) {
        RepTetrazolioViabilidadDTO repeticionCreada = repeticionService.crearRepeticion(tetrazolioId, solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(repeticionCreada);
    }

    
    @Operation(summary = "Listar repeticiones de tetrazolio viabilidad", 
              description = "Obtiene todas las repeticiones asociadas a un análisis de tetrazolio viabilidad específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<List<RepTetrazolioViabilidadDTO>> obtenerRepeticionesPorTetrazolio(@PathVariable Long tetrazolioId) {
        List<RepTetrazolioViabilidadDTO> repeticiones = repeticionService.obtenerRepeticionesPorTetrazolio(tetrazolioId);
        return ResponseEntity.ok(repeticiones);
    }

    
    @Operation(summary = "Contar repeticiones de tetrazolio viabilidad", 
              description = "Cuenta el número de repeticiones asociadas a un análisis de tetrazolio viabilidad específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/count")
    public ResponseEntity<Long> contarRepeticionesPorTetrazolio(@PathVariable Long tetrazolioId) {
        Long count = repeticionService.contarRepeticionesPorTetrazolio(tetrazolioId);
        return ResponseEntity.ok(count);
    }

    
    @Operation(summary = "Obtener repetición de tetrazolio viabilidad por ID", 
              description = "Obtiene una repetición específica de un análisis de tetrazolio viabilidad por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{repeticionId}")
    public ResponseEntity<RepTetrazolioViabilidadDTO> obtenerRepeticionPorId(
            @PathVariable Long tetrazolioId,
            @PathVariable Long repeticionId) {
        RepTetrazolioViabilidadDTO repeticion = repeticionService.obtenerRepeticionPorId(repeticionId);
        return ResponseEntity.ok(repeticion);
    }

    
    @Operation(summary = "Actualizar repetición de tetrazolio viabilidad", 
              description = "Actualiza una repetición específica de un análisis de tetrazolio viabilidad")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{repeticionId}")
    public ResponseEntity<RepTetrazolioViabilidadDTO> actualizarRepeticion(
            @PathVariable Long tetrazolioId,
            @PathVariable Long repeticionId,
            @RequestBody RepTetrazolioViabilidadRequestDTO solicitud) {
        RepTetrazolioViabilidadDTO repeticionActualizada = repeticionService.actualizarRepeticion(repeticionId, solicitud);
        return ResponseEntity.ok(repeticionActualizada);
    }

    
    @Operation(summary = "Eliminar repetición de tetrazolio viabilidad", 
              description = "Elimina una repetición específica de un análisis de tetrazolio viabilidad (eliminación física)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{repeticionId}")
    public ResponseEntity<Void> eliminarRepeticion(
            @PathVariable Long tetrazolioId,
            @PathVariable Long repeticionId) {
        repeticionService.eliminarRepeticion(repeticionId);
        return ResponseEntity.noContent().build();
    }
}