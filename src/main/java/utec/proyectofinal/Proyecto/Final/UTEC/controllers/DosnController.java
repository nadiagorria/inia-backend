package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoDosn;
import utec.proyectofinal.Proyecto.Final.UTEC.services.DosnService;


@RestController
@RequestMapping("/api/dosn")
@Tag(name = "Determinación de Otras Semillas en Número (DOSN)", description = "API para gestión de DOSN")
@SecurityRequirement(name = "bearerAuth")
public class DosnController {

    @Autowired
    private DosnService dosnService;

    
    @Operation(summary = "Crear declaración de origen y sanidad (DOSN)", 
              description = "Crea una nueva declaración de origen y sanidad (DOSN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PostMapping
    public ResponseEntity<DosnDTO> crearDosn(@RequestBody DosnRequestDTO solicitud) {
        DosnDTO dosnCreado = dosnService.crearDosn(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(dosnCreado);
    }

    
    @Operation(summary = "Listar todos los DOSN", 
              description = "Obtiene todos los DOSN activas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<ResponseListadoDosn> obtenerTodasDosnActivas() {
        ResponseListadoDosn respuesta = dosnService.obtenerTodasDosnActivas();
        return ResponseEntity.ok(respuesta);
    }

    
    @Operation(summary = "Obtener declaración de origen y sanidad (DOSN) por ID", 
              description = "Obtiene una declaración de origen y sanidad (DOSN) específica por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<DosnDTO> obtenerDosnPorId(@PathVariable Long id) {
        DosnDTO dosn = dosnService.obtenerDosnPorId(id);
        return ResponseEntity.ok(dosn);
    }

    
    @Operation(summary = "Actualizar declaración de origen y sanidad (DOSN)", 
              description = "Actualiza una declaración de origen y sanidad (DOSN) existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{id}")
    public ResponseEntity<DosnDTO> actualizarDosn(@PathVariable Long id, @RequestBody DosnRequestDTO solicitud) {
        DosnDTO dosnActualizado = dosnService.actualizarDosn(id, solicitud);
        return ResponseEntity.ok(dosnActualizado);
    }

    
    @Operation(summary = "Eliminar análisis de DOSN", 
              description = "Elimina una declaración de origen y sanidad (DOSN) cambiando su estado a INACTIVO")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDosn(@PathVariable Long id) {
        dosnService.eliminarDosn(id);
        return ResponseEntity.noContent().build();
    }

    
    @Operation(summary = "Desactivar análisis DOSN", 
              description = "Desactiva un análisis DOSN (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarDosn(@PathVariable Long id) {
        dosnService.desactivarDosn(id);
        return ResponseEntity.ok().build();
    }

    
    @Operation(summary = "Reactivar análisis DOSN", 
              description = "Reactiva un análisis DOSN desactivado (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<DosnDTO> reactivarDosn(@PathVariable Long id) {
        DosnDTO dosnReactivada = dosnService.reactivarDosn(id);
        return ResponseEntity.ok(dosnReactivada);
    }

    
    @Operation(summary = "Obtener DOSN por ID de lote", 
              description = "Obtiene todos los DOSN asociados a un lote específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<DosnDTO>> obtenerDosnPorIdLote(@PathVariable Integer idLote) {
        List<DosnDTO> dosn = dosnService.obtenerDosnPorIdLote(idLote);
        return ResponseEntity.ok(dosn);
    }


    
    @Operation(summary = "Obtener DOSN paginadas", 
              description = "Obtiene la lista paginada de análisis de DOSN para el listado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnListadoDTO>> obtenerDosnPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean activo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String estado,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long loteId) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnListadoDTO> response = dosnService.obtenerDosnPaginadasConFiltros(pageable, search, activo, estado, loteId);
        return ResponseEntity.ok(response);
    }

    
    @Operation(summary = "Finalizar análisis de DOSN", 
              description = "Finaliza un DOSN según el rol del usuario")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<DosnDTO> finalizarAnalisis(@PathVariable Long id) {
        DosnDTO dosnFinalizada = dosnService.finalizarAnalisis(id);
        return ResponseEntity.ok(dosnFinalizada);
    }

    
    @Operation(summary = "Aprobar análisis de DOSN", 
              description = "Aprueba un DOSN - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<DosnDTO> aprobarAnalisis(@PathVariable Long id) {
        DosnDTO dosnAprobada = dosnService.aprobarAnalisis(id);
        return ResponseEntity.ok(dosnAprobada);
    }

    
    @Operation(summary = "Marcar análisis de DOSN para repetir", 
              description = "Marca un DOSN para repetir - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/repetir")
    public ResponseEntity<DosnDTO> marcarParaRepetir(@PathVariable Long id) {
        DosnDTO dosnRepetir = dosnService.marcarParaRepetir(id);
        return ResponseEntity.ok(dosnRepetir);
    }
    }

