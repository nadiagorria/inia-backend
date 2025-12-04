package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;
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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.MalezasCatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.MalezasCatalogoService;

@RestController
@RequestMapping("/api/malezas")
@Tag(name = "Malezas", description = "API para gestión del catálogo de malezas")
@SecurityRequirement(name = "bearerAuth")
public class MalezasCatalogoController {
    @Autowired
    private MalezasCatalogoService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Listar malezas activas")
    public ResponseEntity<List<MalezasCatalogoDTO>> obtenerTodos() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @GetMapping("/inactivos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar malezas inactivas")
    public ResponseEntity<List<MalezasCatalogoDTO>> obtenerInactivos() {
        return ResponseEntity.ok(service.obtenerInactivos());
    }

    @GetMapping("/buscar/comun")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Buscar por nombre com�n")
    public ResponseEntity<List<MalezasCatalogoDTO>> buscarPorNombreComun(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarPorNombreComun(nombre));
    }

    @GetMapping("/buscar/cientifico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Buscar por nombre cient�fico")
    public ResponseEntity<List<MalezasCatalogoDTO>> buscarPorNombreCientifico(@RequestParam String nombre) {
        return ResponseEntity.ok(service.buscarPorNombreCientifico(nombre));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener por ID")
    public ResponseEntity<MalezasCatalogoDTO> obtenerPorId(@PathVariable Long id) {
        MalezasCatalogoDTO catalogo = service.obtenerPorId(id);
        return catalogo != null ? ResponseEntity.ok(catalogo) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear maleza")
    public ResponseEntity<MalezasCatalogoDTO> crear(@RequestBody MalezasCatalogoRequestDTO solicitud) {
        return ResponseEntity.ok(service.crear(solicitud));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar maleza")
    public ResponseEntity<MalezasCatalogoDTO> actualizar(@PathVariable Long id, @RequestBody MalezasCatalogoRequestDTO solicitud) {
        MalezasCatalogoDTO actualizado = service.actualizar(id, solicitud);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar maleza")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar maleza")
    public ResponseEntity<MalezasCatalogoDTO> reactivar(@PathVariable Long id) {
        MalezasCatalogoDTO reactivado = service.reactivar(id);
        return reactivado != null ? ResponseEntity.ok(reactivado) : ResponseEntity.notFound().build();
    }

    
    @GetMapping("/listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener malezas paginadas", 
              description = "Obtiene la lista paginada de malezas para el listado")
    public ResponseEntity<Page<MalezasCatalogoDTO>> obtenerMalezasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MalezasCatalogoDTO> response = service.obtenerMalezasPaginadasConFiltros(pageable, search, activo);
        return ResponseEntity.ok(response);
    }
}
