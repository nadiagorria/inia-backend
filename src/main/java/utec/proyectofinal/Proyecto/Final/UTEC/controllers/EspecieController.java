package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.EspecieRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EspecieDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.EspecieService;


@RestController
@RequestMapping("/api/especie")
@Tag(name = "Especies", description = "API para gestión de especies")
@SecurityRequirement(name = "bearerAuth")
public class EspecieController {

    @Autowired
    private EspecieService especieService;

    
    @Operation(summary = "Listar todas las especies",
              description = "Obtiene todas las especies con filtro opcional de estado (activo/inactivo)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<List<EspecieDTO>> obtenerTodas(
            @RequestParam(required = false) Boolean activo) {
        List<EspecieDTO> especies = especieService.obtenerTodas(activo);
        return ResponseEntity.ok(especies);
    }

    
    @Operation(summary = "Listar especies inactivas", 
              description = "Obtiene todas las especies inactivas")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inactivas")
    public ResponseEntity<List<EspecieDTO>> obtenerInactivas() {
        List<EspecieDTO> especies = especieService.obtenerInactivas();
        return ResponseEntity.ok(especies);
    }

    
    @Operation(summary = "Buscar especies por nombre común", 
              description = "Busca especies cuyo nombre común contenga el texto proporcionado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/buscar/comun")
    public ResponseEntity<List<EspecieDTO>> buscarPorNombreComun(@RequestParam String nombre) {
        List<EspecieDTO> especies = especieService.buscarPorNombreComun(nombre);
        return ResponseEntity.ok(especies);
    }

    
    @Operation(summary = "Buscar especies por nombre científico", 
              description = "Busca especies cuyo nombre científico contenga el texto proporcionado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/buscar/cientifico")
    public ResponseEntity<List<EspecieDTO>> buscarPorNombreCientifico(@RequestParam String nombre) {
        List<EspecieDTO> especies = especieService.buscarPorNombreCientifico(nombre);
        return ResponseEntity.ok(especies);
    }

    
    @Operation(summary = "Obtener especie por ID", 
              description = "Obtiene una especie específica por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<EspecieDTO> obtenerPorId(@PathVariable Long id) {
        EspecieDTO especie = especieService.obtenerPorId(id);
        if (especie != null) {
            return ResponseEntity.ok(especie);
        }
        return ResponseEntity.notFound().build();
    }

    
    @Operation(summary = "Crear nueva especie", 
              description = "Crea una nueva especie (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EspecieDTO> crear(@RequestBody EspecieRequestDTO solicitud) {
        EspecieDTO creada = especieService.crear(solicitud);
        return ResponseEntity.ok(creada);
    }

    
    @Operation(summary = "Actualizar especie", 
              description = "Actualiza una especie existente (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EspecieDTO> actualizar(@PathVariable Long id, @RequestBody EspecieRequestDTO solicitud) {
        EspecieDTO actualizada = especieService.actualizar(id, solicitud);
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        }
        return ResponseEntity.notFound().build();
    }

    
    @Operation(summary = "Eliminar especie", 
              description = "Elimina una especie (soft delete) (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especieService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    
    @Operation(summary = "Reactivar especie", 
              description = "Reactiva una especie (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<EspecieDTO> reactivar(@PathVariable Long id) {
        EspecieDTO reactivada = especieService.reactivar(id);
        if (reactivada != null) {
            return ResponseEntity.ok(reactivada);
        }
        return ResponseEntity.notFound().build();
    }

    
    @Operation(summary = "Obtener especies paginadas", 
              description = "Obtiene la lista paginada de especies para el listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<Page<EspecieDTO>> obtenerEspeciesPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EspecieDTO> response = especieService.obtenerEspeciesPaginadasConFiltros(pageable, search, activo);
        return ResponseEntity.ok(response);
    }
}