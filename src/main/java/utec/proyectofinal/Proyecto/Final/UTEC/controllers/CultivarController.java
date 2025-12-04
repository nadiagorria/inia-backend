package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CultivarRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CultivarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.CultivarService;

import java.util.List;


@RestController
@RequestMapping("/api/cultivar")
@Tag(name = "Cultivares", description = "API para gestión de cultivares")
@SecurityRequirement(name = "bearerAuth")
public class CultivarController {

    @Autowired
    private CultivarService cultivarService;

    
    @Operation(summary = "Listar todos los cultivares", 
              description = "Obtiene todos los cultivares con filtro opcional de estado (activo/inactivo)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<List<CultivarDTO>> obtenerTodos(
            @RequestParam(required = false) Boolean activo) {
        List<CultivarDTO> cultivares = cultivarService.obtenerTodos(activo);
        return ResponseEntity.ok(cultivares);
    }

    
    @Operation(summary = "Listar todos los cultivares inactivos", 
              description = "Obtiene todos los cultivares que están inactivos en el sistema")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inactivos")
    public ResponseEntity<List<CultivarDTO>> obtenerInactivos() {
        List<CultivarDTO> cultivares = cultivarService.obtenerInactivos();
        return ResponseEntity.ok(cultivares);
    }

    
    @Operation(summary = "Listar todos los cultivares por especie", 
              description = "Obtiene todos los cultivares asociados a una especie específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/especie/{especieID}")
    public ResponseEntity<List<CultivarDTO>> obtenerPorEspecie(@PathVariable Long especieID) {
        List<CultivarDTO> cultivares = cultivarService.obtenerPorEspecie(especieID);
        return ResponseEntity.ok(cultivares);
    }

    
    @Operation(summary = "Buscar cultivares por nombre", 
              description = "Busca cultivares cuyo nombre contenga el texto especificado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/buscar")
    public ResponseEntity<List<CultivarDTO>> buscarPorNombre(@RequestParam String nombre) {
        List<CultivarDTO> cultivares = cultivarService.buscarPorNombre(nombre);
        return ResponseEntity.ok(cultivares);
    }

    
    @Operation(summary = "Obtener cultivar por ID", 
              description = "Obtiene un cultivar específico por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<CultivarDTO> obtenerPorId(@PathVariable Long id) {
        CultivarDTO cultivar = cultivarService.obtenerPorId(id);
        if (cultivar != null) {
            return ResponseEntity.ok(cultivar);
        }
        return ResponseEntity.notFound().build();
    }

    
    @Operation(summary = "Crear nuevo cultivar", 
              description = "Crea un nuevo cultivar en el sistema")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CultivarDTO> crear(@RequestBody CultivarRequestDTO solicitud) {
        try {
            CultivarDTO creado = cultivarService.crear(solicitud);
            return ResponseEntity.ok(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    
    @Operation(summary = "Actualizar cultivar", 
              description = "Actualiza los detalles de un cultivar existente")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CultivarDTO> actualizar(@PathVariable Long id, @RequestBody CultivarRequestDTO solicitud) {
        try {
            CultivarDTO actualizado = cultivarService.actualizar(id, solicitud);
            if (actualizado != null) {
                return ResponseEntity.ok(actualizado);
            }
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    
    @Operation(summary = "Eliminar cultivar", 
              description = "Elimina (cambia a inactivo) un cultivar existente")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cultivarService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    
    @Operation(summary = "Reactivar cultivar", 
              description = "Reactiva un cultivar que estaba inactivo")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<CultivarDTO> reactivar(@PathVariable Long id) {
        CultivarDTO reactivado = cultivarService.reactivar(id);
        if (reactivado != null) {
            return ResponseEntity.ok(reactivado);
        }
        return ResponseEntity.notFound().build();
    }

    
    @Operation(summary = "Obtener cultivares paginados", 
              description = "Obtiene la lista paginada de cultivares para el listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<Page<CultivarDTO>> obtenerCultivaresPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CultivarDTO> response = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, search, activo);
        return ResponseEntity.ok(response);
    }
}