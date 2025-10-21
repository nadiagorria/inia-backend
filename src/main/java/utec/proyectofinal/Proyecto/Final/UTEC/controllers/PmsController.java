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

// CORS configurado globalmente en WebSecurityConfig
@RestController
@RequestMapping("/api/pms")
@Tag(name = "PMS", description = "API para gestión del análisis de Peso de Mil Semillas")
@SecurityRequirement(name = "bearerAuth")
public class PmsController {

    @Autowired
    private PmsService pmsService;

    // Crear nuevo Pms
    @Operation(summary = "Crear análisis de peso de mil semillas (PMS)", 
              description = "Crea un nuevo análisis de peso de mil semillas (PMS)")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PmsDTO> crearPms(@RequestBody PmsRequestDTO solicitud) {
        try {
            PmsDTO creado = pmsService.crearPms(solicitud);
            return new ResponseEntity<>(creado, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todos los Pms activos
    @Operation(summary = "Listar todos los análisis de peso de mil semillas (PMS)", 
              description = "Obtiene todos los análisis de peso de mil semillas (PMS) activos")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<List<PmsDTO>> obtenerTodos() {
        try {
            List<PmsDTO> lista = pmsService.obtenerTodos();
            return new ResponseEntity<>(lista, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Pms por ID
    @Operation(summary = "Obtener análisis de peso de mil semillas (PMS) por ID", 
              description = "Obtiene un análisis de peso de mil semillas (PMS) específico por su ID")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<PmsDTO> obtenerPorId(@PathVariable Long id) {
        try {
            PmsDTO dto = pmsService.obtenerPorId(id);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Obtener PMS con paginado para listado
    @Operation(summary = "Obtener PMS paginadas", 
              description = "Obtiene la lista paginada de análisis de PMS para el listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsListadoDTO>> obtenerPmsPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        try {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
            org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsListadoDTO> response = pmsService.obtenerPmsPaginadas(pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Pms
    @Operation(summary = "Actualizar análisis de peso de mil semillas (PMS)", 
              description = "Actualiza un análisis de peso de mil semillas (PMS) existente")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PmsDTO> actualizarPms(@PathVariable Long id, @RequestBody PmsRequestDTO solicitud) {
        try {
            PmsDTO actualizado = pmsService.actualizarPms(id, solicitud);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Pms (cambiar estado a INACTIVO)
    @Operation(summary = "Eliminar análisis de peso de mil semillas (PMS)", 
              description = "Elimina (cambia a inactivo) un análisis de peso de mil semillas (PMS) existente")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarPms(@PathVariable Long id) {
        try {
            pmsService.eliminarPms(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Pms por Lote
    @Operation(summary = "Obtener análisis de peso de mil semillas (PMS) por ID de lote", 
              description = "Obtiene todos los análisis de peso de mil semillas (PMS) asociados a un lote específico")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<PmsDTO>> obtenerPmsPorIdLote(@PathVariable Long idLote) {
        try {
            List<PmsDTO> lista = pmsService.obtenerPmsPorIdLote(idLote);
            return new ResponseEntity<>(lista, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar PMS con valor redondeado (solo cuando todas las repeticiones estén completas)
    @Operation(summary = "Actualizar análisis de peso de mil semillas (PMS) con redondeo", 
              description = "Actualiza un análisis de peso de mil semillas (PMS) existente con valores redondeados")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/redondeo")
    public ResponseEntity<PmsDTO> actualizarPmsConRedondeo(@PathVariable Long id, @RequestBody PmsRedondeoRequestDTO solicitud) {
        try {
            PmsDTO actualizado = pmsService.actualizarPmsConRedondeo(id, solicitud);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Finalizar análisis PMS
    @Operation(summary = "Finalizar análisis de peso de mil semillas (PMS)", 
              description = "Finaliza un análisis de peso de mil semillas (PMS), marcándolo como completado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<PmsDTO> finalizarAnalisis(@PathVariable Long id) {
        try {
            PmsDTO analisisFinalizado = pmsService.finalizarAnalisis(id);
            return new ResponseEntity<>(analisisFinalizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Aprobar análisis (solo admin)
    @Operation(summary = "Aprobar análisis de peso de mil semillas (PMS)", 
              description = "Aprueba un análisis de peso de mil semillas (PMS) - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<PmsDTO> aprobarAnalisis(@PathVariable Long id) {
        try {
            PmsDTO analisisAprobado = pmsService.aprobarAnalisis(id);
            return new ResponseEntity<>(analisisAprobado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Marcar análisis para repetir (solo admin)
    @Operation(summary = "Marcar análisis de peso de mil semillas (PMS) para repetir", 
              description = "Marca un análisis de peso de mil semillas (PMS) para repetir - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/repetir")
    public ResponseEntity<PmsDTO> marcarParaRepetir(@PathVariable Long id) {
        try {
            PmsDTO analisisRepetir = pmsService.marcarParaRepetir(id);
            return new ResponseEntity<>(analisisRepetir, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
