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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TablaGermService;


@RestController
@RequestMapping("/api/germinacion/{germinacionId}/tabla")
@Tag(name = "Tabla de Germinación", description = "API para gestión de tablas dentro del análisis de germinación")
@SecurityRequirement(name = "bearerAuth")
public class TablaGermController {

    @Autowired
    private TablaGermService tablaGermService;

    
    @Operation(summary = "Crear tabla de germinación", 
              description = "Crea una nueva tabla de germinación. Solo se puede crear si todas las tablas anteriores están finalizadas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tabla creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede crear - tabla anterior no finalizada o datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Germinación no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PostMapping
    public ResponseEntity<?> crearTablaGerm(
            @PathVariable Long germinacionId,
            @RequestBody TablaGermRequestDTO solicitud) {
        try {
            TablaGermDTO nuevaTablaGerm = tablaGermService.crearTablaGerm(germinacionId, solicitud);
            return new ResponseEntity<>(nuevaTablaGerm, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @Operation(summary = "Contar tablas por germinación", description = "Devuelve el número total de tablas asociadas a una germinación específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/contar")
    public ResponseEntity<?> contarTablasPorGerminacion(@PathVariable Long germinacionId) {
        try {
            Long count = tablaGermService.contarTablasPorGerminacion(germinacionId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @Operation(summary = "Finalizar tabla de germinación",
              description = "Marca una tabla como finalizada. Requiere que todas las repeticiones estén completas y porcentajes ingresados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tabla finalizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede finalizar - faltan repeticiones o porcentajes"),
        @ApiResponse(responseCode = "404", description = "Tabla no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{tablaId}/finalizar")
    public ResponseEntity<?> finalizarTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            TablaGermDTO tablaFinalizada = tablaGermService.finalizarTabla(tablaId);
            return new ResponseEntity<>(tablaFinalizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    
    @Operation(summary = "Verificar si se pueden ingresar porcentajes", 
              description = "Verifica si es posible ingresar los 5 porcentajes con redondeo para una tabla. Solo si todas las repeticiones están completas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @GetMapping("/{tablaId}/puede-ingresar-porcentajes")
    public ResponseEntity<?> puedeIngresarPorcentajes(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            boolean puede = tablaGermService.puedeIngresarPorcentajes(tablaId);
            return new ResponseEntity<>(puede, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @Operation(summary = "Actualizar porcentajes con redondeo", 
              description = "Actualiza los 5 porcentajes con redondeo de una tabla. Solo disponible cuando todas las repeticiones están completas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Porcentajes actualizados exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se pueden ingresar porcentajes - repeticiones incompletas"),
        @ApiResponse(responseCode = "404", description = "Tabla no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{tablaId}/porcentajes")
    public ResponseEntity<?> actualizarPorcentajes(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @RequestBody PorcentajesRedondeoRequestDTO solicitud) {
        try {
            TablaGermDTO tablaActualizada = tablaGermService.actualizarPorcentajes(tablaId, solicitud);
            return new ResponseEntity<>(tablaActualizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    } 

    
    @Operation(summary = "Obtener tabla por ID", 
              description = "Obtiene los detalles de una tabla de germinación específica dentro de una germinación")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{tablaId}")
    public ResponseEntity<?> obtenerTablaGermPorId(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            TablaGermDTO tablaGerm = tablaGermService.obtenerTablaGermPorId(tablaId);
            return new ResponseEntity<>(tablaGerm, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Actualizar tabla de germinación", 
              description = "Actualiza los detalles de una tabla de germinación existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{tablaId}")
    public ResponseEntity<?> actualizarTablaGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @RequestBody TablaGermRequestDTO solicitud) {
        try {
            TablaGermDTO tablaGermActualizada = tablaGermService.actualizarTablaGerm(tablaId, solicitud);
            return new ResponseEntity<>(tablaGermActualizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Eliminar tabla de germinación", 
              description = "Elimina una tabla de germinación específica (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{tablaId}")
    public ResponseEntity<?> eliminarTablaGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            tablaGermService.eliminarTablaGerm(tablaId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Obtener tablas por germinación", 
              description = "Obtiene todas las tablas asociadas a una germinación específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<?> obtenerTablasPorGerminacion(@PathVariable Long germinacionId) {
        try {
            List<TablaGermDTO> tablas = tablaGermService.obtenerTablasPorGerminacion(germinacionId);
            return new ResponseEntity<>(tablas, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}