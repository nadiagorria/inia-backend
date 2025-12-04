package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.CatalogoService;


@RestController
@RequestMapping("/api/catalogo")
@Tag(name = "Catálogos", description = "API para gestión de catálogos del sistema")
@SecurityRequirement(name = "bearerAuth")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

    
    @GetMapping
    @Operation(summary = "Listar catálogos", description = "Obtiene todos los catálogos activos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<CatalogoDTO>> obtenerTodos() {
        List<CatalogoDTO> catalogos = catalogoService.obtenerTodos();
        return ResponseEntity.ok(catalogos);
    }

    
    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar catálogos por tipo", description = "Obtiene catálogos filtrados por tipo y estado (activo/inactivo)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<CatalogoDTO>> obtenerPorTipo(
            @PathVariable String tipo,
            @RequestParam(required = false) Boolean activo) {
        try {
            List<CatalogoDTO> catalogos = catalogoService.obtenerPorTipo(tipo, activo);
            return ResponseEntity.ok(catalogos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener catálogo por ID", description = "Obtiene un catálogo específico por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<CatalogoDTO> obtenerPorId(@PathVariable Long id) {
        CatalogoDTO catalogo = catalogoService.obtenerPorId(id);
        if (catalogo != null) {
            return ResponseEntity.ok(catalogo);
        }
        return ResponseEntity.notFound().build();
    }

    
    @PostMapping
    @Operation(summary = "Crear catálogo", description = "Crea un nuevo catálogo (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@RequestBody CatalogoRequestDTO solicitud) {
        try {
            CatalogoDTO creado = catalogoService.crear(solicitud);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Tipo de catálogo inválido: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar catálogo", description = "Actualiza un catálogo existente (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody CatalogoRequestDTO solicitud) {
        try {
            CatalogoDTO actualizado = catalogoService.actualizar(id, solicitud);
            if (actualizado != null) {
                return ResponseEntity.ok(actualizado);
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Tipo de catálogo inválido: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar catálogo", description = "Desactiva un catálogo (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        catalogoService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    
    @Operation(summary = "Reactivar catálogo", description = "Reactiva un catálogo desactivado (solo administradores)")
    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatalogoDTO> reactivar(@PathVariable Long id) {
        CatalogoDTO reactivado = catalogoService.reactivar(id);
        if (reactivado != null) {
            return ResponseEntity.ok(reactivado);
        }
        return ResponseEntity.notFound().build();
    }

    
    @Operation(summary = "Eliminar catálogo físicamente", description = "Elimina un catálogo de forma permanente (solo administradores)")
    @DeleteMapping("/{id}/fisico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarFisicamente(@PathVariable Long id) {
        catalogoService.eliminarFisicamente(id);
        return ResponseEntity.ok().build();
    }

    
    @Operation(summary = "Obtener Catalogo Humedad")
    @GetMapping("/humedad")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<CatalogoDTO>> obtenerTiposHumedad() {
        return obtenerPorTipo("HUMEDAD", true);
    }

    @Operation(summary = "Obtener Catalogo Numero de Articulos")
    @GetMapping("/articulos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<CatalogoDTO>> obtenerNumerosArticulo() {
        return obtenerPorTipo("ARTICULO", true);
    }

    @Operation(summary = "Obtener Catalogo Origen")
    @GetMapping("/origenes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<CatalogoDTO>> obtenerOrigenes() {
        return obtenerPorTipo("ORIGEN", true);
    }

    @Operation(summary = "Obtener Catalogo Estados")
    @GetMapping("/estados")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<CatalogoDTO>> obtenerEstados() {
        return obtenerPorTipo("ESTADO", true);
    }

    @Operation(summary = "Obtener Catalogo Deposito")
    @GetMapping("/depositos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<CatalogoDTO>> obtenerDepositos() {
        return obtenerPorTipo("DEPOSITO", true);
    }

    @Operation(summary = "Obtener Catalogo Unidades de Embolsado")
    @GetMapping("/unidades-embolsado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<List<CatalogoDTO>> obtenerUnidadesEmbolsado() {
        return obtenerPorTipo("UNIDAD_EMBOLSADO", true);
    }

    
    @Operation(summary = "Obtener catálogos paginados", 
              description = "Obtiene la lista paginada de catálogos para el listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<Page<CatalogoDTO>> obtenerCatalogosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String tipo) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CatalogoDTO> response = catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, search, activo, tipo);
        return ResponseEntity.ok(response);
    }
}