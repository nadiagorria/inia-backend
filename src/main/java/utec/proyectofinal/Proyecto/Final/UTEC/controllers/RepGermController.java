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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepGermService;


@RestController
@RequestMapping("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion")
@Tag(name = "Repeticiones de Germinación", description = "API para gestión de repeticiones dentro de las tablas de germinación")
@SecurityRequirement(name = "bearerAuth")
public class RepGermController {

    @Autowired
    private RepGermService repGermService;

    
    @Operation(summary = "Crear repetición", 
              description = "Crea una nueva repetición para una tabla. No puede superar el numeroRepeticiones definido en la germinación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Repetición creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Máximo de repeticiones alcanzado o tabla finalizada"),
        @ApiResponse(responseCode = "404", description = "Tabla no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PostMapping
    public ResponseEntity<?> crearRepGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @RequestBody RepGermRequestDTO solicitud) {
        try {
            RepGermDTO nuevaRepeticion = repGermService.crearRepGerm(tablaId, solicitud);
            return new ResponseEntity<>(nuevaRepeticion, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    
    @Operation(summary = "Obtener repetición por ID", description = "Devuelve una repetición específica por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{repeticionId}")
    public ResponseEntity<?> obtenerRepGermPorId(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long repeticionId) {
        try {
            RepGermDTO repeticion = repGermService.obtenerRepGermPorId(repeticionId);
            return new ResponseEntity<>(repeticion, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Actualizar repetición", description = "Actualiza los datos de una repetición existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{repeticionId}")
    public ResponseEntity<?> actualizarRepGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long repeticionId,
            @RequestBody RepGermRequestDTO solicitud) {
        try {
            RepGermDTO repeticionActualizada = repGermService.actualizarRepGerm(repeticionId, solicitud);
            return new ResponseEntity<>(repeticionActualizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Eliminar repetición", description = "Elimina una repetición específica por su ID")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{repeticionId}")
    public ResponseEntity<?> eliminarRepGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long repeticionId) {
        try {
            repGermService.eliminarRepGerm(repeticionId);
            return new ResponseEntity<>("Repetición eliminada exitosamente", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Obtener repeticiones por tabla", description = "Devuelve todas las repeticiones asociadas a una tabla específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<?> obtenerRepeticionesPorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            List<RepGermDTO> repeticiones = repGermService.obtenerRepeticionesPorTabla(tablaId);
            return new ResponseEntity<>(repeticiones, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    
    @Operation(summary = "Contar repeticiones por tabla", description = "Devuelve el número total de repeticiones asociadas a una tabla específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/contar")
    public ResponseEntity<?> contarRepeticionesPorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            Long cantidad = repGermService.contarRepeticionesPorTabla(tablaId);
            return new ResponseEntity<>(cantidad, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}