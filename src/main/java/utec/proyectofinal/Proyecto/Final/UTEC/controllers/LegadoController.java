package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.LegadoService;



@RestController
@RequestMapping("/api/legados")
@Tag(name = "Legados", description = "API para gestión de datos históricos legados")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class LegadoController {

    private final LegadoService legadoService;

    
    @GetMapping
    @Operation(summary = "Listar legados", description = "Obtiene todos los registros legados activos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<List<LegadoSimpleDTO>> listarTodos() {
        try {
            List<LegadoSimpleDTO> legados = legadoService.obtenerTodosSimple();
            return ResponseEntity.ok(legados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @GetMapping("/listado")
    @Operation(summary = "Listar legados paginados", description = "Obtiene legados paginados con filtros de búsqueda")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<Page<LegadoListadoDTO>> obtenerLegadosPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String especie,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReciboInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReciboFin) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LegadoListadoDTO> response = legadoService.obtenerLegadosPaginadas(
                pageable, search, especie, fechaReciboInicio, fechaReciboFin);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @GetMapping("/especies")
    @Operation(summary = "Obtener especies únicas", description = "Obtiene todas las especies únicas de los legados activos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<String>> obtenerEspeciesUnicas() {
        try {
            List<String> especies = legadoService.obtenerEspeciesUnicas();
            return ResponseEntity.ok(especies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener legado", description = "Obtiene un registro legado por ID con información completa del lote")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<LegadoDTO> obtenerPorId(@PathVariable Long id) {
        try {
            LegadoDTO legado = legadoService.obtenerPorId(id);
            return ResponseEntity.ok(legado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @GetMapping("/archivo/{nombreArchivo}")
    @Operation(summary = "Buscar por archivo", description = "Obtiene registros legados de un archivo específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<List<LegadoSimpleDTO>> obtenerPorArchivo(@PathVariable String nombreArchivo) {
        try {
            List<LegadoSimpleDTO> legados = legadoService.obtenerPorArchivo(nombreArchivo);
            return ResponseEntity.ok(legados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @GetMapping("/ficha/{ficha}")
    @Operation(summary = "Buscar por ficha", description = "Obtiene registros legados de una ficha específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<List<LegadoSimpleDTO>> obtenerPorFicha(@PathVariable String ficha) {
        try {
            List<LegadoSimpleDTO> legados = legadoService.obtenerPorFicha(ficha);
            return ResponseEntity.ok(legados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar legado", description = "Desactiva un registro legado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        try {
            legadoService.desactivar(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
