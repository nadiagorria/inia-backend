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

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepPmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepPmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepPmsService;


@RestController
@RequestMapping("/api/pms/{pmsId}/repeticiones")
@Tag(name = "Repeticiones PMS", description = "API para gestión de repeticiones de análisis PMS")
@SecurityRequirement(name = "bearerAuth")
public class RepPmsController {

    @Autowired
    private RepPmsService repeticionService;

    
    @Operation(summary = "Crear repetición de PMS", 
              description = "Crea una nueva repetición para un análisis PMS específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PostMapping
    public ResponseEntity<RepPmsDTO> crearRepeticion(
            @PathVariable Long pmsId,
            @RequestBody RepPmsRequestDTO solicitud) {
        try {
            RepPmsDTO repeticionCreada = repeticionService.crearRepeticion(pmsId, solicitud);
            return new ResponseEntity<>(repeticionCreada, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @Operation(summary = "Listar repeticiones de PMS", 
              description = "Obtiene todas las repeticiones asociadas a un análisis PMS específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<List<RepPmsDTO>> obtenerRepeticionesPorPms(@PathVariable Long pmsId) {
        try {
            List<RepPmsDTO> repeticiones = repeticionService.obtenerPorPms(pmsId);
            return new ResponseEntity<>(repeticiones, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @Operation(summary = "Contar repeticiones de PMS", 
              description = "Cuenta el número de repeticiones asociadas a un análisis PMS específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/count")
    public ResponseEntity<Long> contarRepeticionesPorPms(@PathVariable Long pmsId) {
        try {
            Long count = repeticionService.contarPorPms(pmsId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @Operation(summary = "Obtener repetición de PMS por ID", 
              description = "Obtiene una repetición específica de un análisis PMS por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{repeticionId}")
    public ResponseEntity<RepPmsDTO> obtenerRepeticionPorId(
            @PathVariable Long pmsId,
            @PathVariable Long repeticionId) {
        try {
            RepPmsDTO repeticion = repeticionService.obtenerPorId(repeticionId);
            return new ResponseEntity<>(repeticion, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @Operation(summary = "Actualizar repetición de PMS", 
              description = "Actualiza una repetición específica de un análisis PMS")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{repeticionId}")
    public ResponseEntity<RepPmsDTO> actualizarRepeticion(
            @PathVariable Long pmsId,
            @PathVariable Long repeticionId,
            @RequestBody RepPmsRequestDTO solicitud) {
        try {
            RepPmsDTO repeticionActualizada = repeticionService.actualizarRepeticion(repeticionId, solicitud);
            return new ResponseEntity<>(repeticionActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @Operation(summary = "Eliminar repetición de PMS", 
              description = "Elimina una repetición específica de un análisis PMS (eliminación física)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{repeticionId}")
    public ResponseEntity<HttpStatus> eliminarRepeticion(
            @PathVariable Long pmsId,
            @PathVariable Long repeticionId) {
        try {
            repeticionService.eliminarRepeticion(repeticionId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
