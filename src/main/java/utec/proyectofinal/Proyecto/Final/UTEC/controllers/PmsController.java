package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PmsService;


@RestController
@RequestMapping("/api/pms")
@Tag(name = "PMS", description = "API para gestión del análisis de Peso de Mil Semillas")
@SecurityRequirement(name = "bearerAuth")
public class PmsController {

    @Autowired
    private PmsService pmsService;

    
    @Operation(summary = "Crear análisis de peso de mil semillas (PMS)", 
              description = "Crea un nuevo análisis de peso de mil semillas (PMS)")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PmsDTO> crearPms(@RequestBody PmsRequestDTO solicitud) {
        PmsDTO creado = pmsService.crearPms(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    
    @Operation(summary = "Listar todos los análisis de peso de mil semillas (PMS)", 
              description = "Obtiene todos los análisis de peso de mil semillas (PMS) activos")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<List<PmsDTO>> obtenerTodos() {
        List<PmsDTO> lista = pmsService.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    
    @Operation(summary = "Obtener análisis de peso de mil semillas (PMS) por ID", 
              description = "Obtiene un análisis de peso de mil semillas (PMS) específico por su ID")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<PmsDTO> obtenerPorId(@PathVariable Long id) {
        PmsDTO dto = pmsService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }
    
    
    @Operation(summary = "Obtener PMS paginadas", 
              description = "Obtiene la lista paginada de análisis de PMS para el listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsListadoDTO>> obtenerPmsPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean activo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String estado,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long loteId) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsListadoDTO> response = pmsService.obtenerPmsPaginadasConFiltros(pageable, search, activo, estado, loteId);
        return ResponseEntity.ok(response);
    }

    
    @Operation(summary = "Actualizar análisis de peso de mil semillas (PMS)", 
              description = "Actualiza un análisis de peso de mil semillas (PMS) existente")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PmsDTO> actualizarPms(@PathVariable Long id, @RequestBody PmsRequestDTO solicitud) {
        PmsDTO actualizado = pmsService.actualizarPms(id, solicitud);
        return ResponseEntity.ok(actualizado);
    }

    
    @Operation(summary = "Eliminar análisis de peso de mil semillas (PMS)", 
              description = "Elimina (cambia a inactivo) un análisis de peso de mil semillas (PMS) existente")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPms(@PathVariable Long id) {
        pmsService.eliminarPms(id);
        return ResponseEntity.noContent().build();
    }

    
    @Operation(summary = "Desactivar análisis PMS", 
              description = "Desactiva un análisis PMS (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarPms(@PathVariable Long id) {
        pmsService.desactivarPms(id);
        return ResponseEntity.ok().build();
    }

    
    @Operation(summary = "Reactivar análisis PMS", 
              description = "Reactiva un análisis PMS desactivado (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<PmsDTO> reactivarPms(@PathVariable Long id) {
        PmsDTO pmsReactivado = pmsService.reactivarPms(id);
        return ResponseEntity.ok(pmsReactivado);
    }

    
    @Operation(summary = "Obtener análisis de peso de mil semillas (PMS) por ID de lote", 
              description = "Obtiene todos los análisis de peso de mil semillas (PMS) asociados a un lote específico")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<PmsDTO>> obtenerPmsPorIdLote(@PathVariable Long idLote) {
        List<PmsDTO> lista = pmsService.obtenerPmsPorIdLote(idLote);
        return ResponseEntity.ok(lista);
    }

    
    @Operation(summary = "Actualizar análisis de peso de mil semillas (PMS) con redondeo", 
              description = "Actualiza un análisis de peso de mil semillas (PMS) existente con valores redondeados")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/redondeo")
    public ResponseEntity<PmsDTO> actualizarPmsConRedondeo(@PathVariable Long id, @RequestBody PmsRedondeoRequestDTO solicitud) {
        PmsDTO actualizado = pmsService.actualizarPmsConRedondeo(id, solicitud);
        return ResponseEntity.ok(actualizado);
    }

    
    @Operation(summary = "Finalizar análisis de peso de mil semillas (PMS)", 
              description = "Finaliza un análisis de peso de mil semillas (PMS), marcándolo como completado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<PmsDTO> finalizarAnalisis(@PathVariable Long id) {
        PmsDTO analisisFinalizado = pmsService.finalizarAnalisis(id);
        return ResponseEntity.ok(analisisFinalizado);
    }

    
    @Operation(summary = "Aprobar análisis de peso de mil semillas (PMS)", 
              description = "Aprueba un análisis de peso de mil semillas (PMS) - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<PmsDTO> aprobarAnalisis(@PathVariable Long id) {
        PmsDTO analisisAprobado = pmsService.aprobarAnalisis(id);
        return ResponseEntity.ok(analisisAprobado);
    }

    
    @Operation(summary = "Marcar análisis de peso de mil semillas (PMS) para repetir", 
              description = "Marca un análisis de peso de mil semillas (PMS) para repetir - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/repetir")
    public ResponseEntity<PmsDTO> marcarParaRepetir(@PathVariable Long id) {
        PmsDTO analisisRepetir = pmsService.marcarParaRepetir(id);
        return ResponseEntity.ok(analisisRepetir);
    }
    }


