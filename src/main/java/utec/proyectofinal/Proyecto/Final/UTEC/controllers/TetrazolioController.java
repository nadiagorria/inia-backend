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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeadosRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TetrazolioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoTetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TetrazolioService;


@RestController
@RequestMapping("/api/tetrazolios")
@Tag(name = "Tetrazolio", description = "API para gestión del análisis de tetrazolio")
@SecurityRequirement(name = "bearerAuth")
public class TetrazolioController {

    @Autowired
    private TetrazolioService tetrazolioService;

    
    @Operation(summary = "Crear análisis de tetrazolio", 
              description = "Crea un nuevo análisis de tetrazolio con numeroRepeticiones y numeroConteos definidos")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TetrazolioDTO> crearTetrazolio(@RequestBody TetrazolioRequestDTO solicitud) {
        TetrazolioDTO tetrazolioCreado = tetrazolioService.crearTetrazolio(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(tetrazolioCreado);
    }

    
    @Operation(summary = "Listar todos los análisis de tetrazolio", 
              description = "Obtiene todos los análisis de tetrazolio activos en el sistema")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<ResponseListadoTetrazolio> obtenerTodosTetrazolio() {
        ResponseListadoTetrazolio response = tetrazolioService.obtenerTodosTetrazolio();
        return ResponseEntity.ok(response);
    }

    
    @Operation(summary = "Obtener análisis de tetrazolio por ID", 
              description = "Obtiene un análisis de tetrazolio específico por su ID")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<TetrazolioDTO> obtenerTetrazolioPorId(@PathVariable Long id) {
        TetrazolioDTO tetrazolio = tetrazolioService.obtenerTetrazolioPorId(id);
        return ResponseEntity.ok(tetrazolio);
    }

    
    @Operation(summary = "Actualizar análisis de tetrazolio", 
              description = "Actualiza los detalles de un análisis de tetrazolio existente")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TetrazolioDTO> actualizarTetrazolio(@PathVariable Long id, @RequestBody TetrazolioRequestDTO solicitud) {
        TetrazolioDTO tetrazolioActualizado = tetrazolioService.actualizarTetrazolio(id, solicitud);
        return ResponseEntity.ok(tetrazolioActualizado);
    }

    
    @Operation(summary = "Actualizar porcentajes redondeados")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/porcentajes")
    public ResponseEntity<TetrazolioDTO> actualizarPorcentajesRedondeados(@PathVariable Long id, @RequestBody PorcentajesRedondeadosRequestDTO solicitud) {
        TetrazolioDTO tetrazolioActualizado = tetrazolioService.actualizarPorcentajesRedondeados(id, solicitud);
        return ResponseEntity.ok(tetrazolioActualizado);
    }

    
    @Operation(summary = "Eliminar análisis de tetrazolio")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTetrazolio(@PathVariable Long id) {
        tetrazolioService.desactivarTetrazolio(id);
        return ResponseEntity.noContent().build();
    }

    
    @Operation(summary = "Desactivar análisis Tetrazolio", 
              description = "Desactiva un análisis Tetrazolio (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarTetrazolio(@PathVariable Long id) {
        tetrazolioService.desactivarTetrazolio(id);
        return ResponseEntity.ok().build();
    }

    
    @Operation(summary = "Reactivar análisis Tetrazolio", 
              description = "Reactiva un análisis Tetrazolio desactivado (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<TetrazolioDTO> reactivarTetrazolio(@PathVariable Long id) {
        TetrazolioDTO tetrazolioReactivado = tetrazolioService.reactivarTetrazolio(id);
        return ResponseEntity.ok(tetrazolioReactivado);
    }

    
    @Operation(summary = "Obtener tetrazolios por lote", 
              description = "Obtiene todos los análisis de tetrazolio asociados a un lote específico")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<TetrazolioDTO>> obtenerTetrazoliosPorIdLote(@PathVariable Long idLote) {
        List<TetrazolioDTO> tetrazolios = tetrazolioService.obtenerTetrazoliosPorIdLote(idLote);
        return ResponseEntity.ok(tetrazolios);
    }

    
    @Operation(summary = "Obtener tetrazolios paginadas", 
              description = "Obtiene la lista paginada de análisis de tetrazolio para el listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioListadoDTO>> obtenerTetrazoliosPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean activo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String estado,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long loteId) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioListadoDTO> response = tetrazolioService.obtenerTetrazoliosPaginadasConFiltros(pageable, search, activo, estado, loteId);
        return ResponseEntity.ok(response);
    }

    
    @Operation(summary = "Finalizar análisis de tetrazolio", 
              description = "Finaliza un análisis de tetrazolio cambiando su estado según el rol del usuario (analista -> PENDIENTE_APROBACION, admin -> APROBADO)")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<TetrazolioDTO> finalizarAnalisis(@PathVariable Long id) {
        TetrazolioDTO tetrazolioFinalizado = tetrazolioService.finalizarAnalisis(id);
        return ResponseEntity.ok(tetrazolioFinalizado);
    }

    
    @Operation(summary = "Aprobar análisis de tetrazolio", 
              description = "Aprueba un análisis de tetrazolio, cambiando su estado a APROBADO (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<TetrazolioDTO> aprobarAnalisis(@PathVariable Long id) {
        TetrazolioDTO tetrazolioAprobado = tetrazolioService.aprobarAnalisis(id);
        return ResponseEntity.ok(tetrazolioAprobado);
    }

    
    @Operation(summary = "Marcar análisis de tetrazolio para repetir", 
              description = "Marca un análisis de tetrazolio para repetir - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/repetir")
    public ResponseEntity<TetrazolioDTO> marcarParaRepetir(@PathVariable Long id) {
        TetrazolioDTO tetrazolioRepetir = tetrazolioService.marcarParaRepetir(id);
        return ResponseEntity.ok(tetrazolioRepetir);
    }
    }
