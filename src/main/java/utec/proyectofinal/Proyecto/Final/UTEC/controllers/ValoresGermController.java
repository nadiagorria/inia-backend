package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ValoresGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ValoresGermService;


@RestController
@RequestMapping("/api/germinacion/{germinacionId}/tabla/{tablaId}/valores")
@Tag(name = "Valores Germinación", description = "API para gestión de valores de germinación")
@SecurityRequirement(name = "bearerAuth")
public class ValoresGermController {

    @Autowired
    private ValoresGermService valoresGermService;

    
    @Operation(summary = "Obtener valores de germinación por ID",
              description = "Obtiene los valores específicos de germinación por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{valoresId}")
    public ResponseEntity<?> obtenerValoresPorId(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long valoresId) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresPorId(valoresId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Actualizar valores de germinación", 
              description = "Actualiza los valores específicos de germinación por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{valoresId}")
    public ResponseEntity<?> actualizarValores(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long valoresId,
            @RequestBody ValoresGermRequestDTO solicitud) {
        try {
            ValoresGermDTO valoresActualizados = valoresGermService.actualizarValores(valoresId, solicitud);
            return new ResponseEntity<>(valoresActualizados, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Obtener todos los valores de germinación para una tabla", 
              description = "Obtiene todos los valores de germinación asociados a una tabla específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<?> obtenerValoresPorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            List<ValoresGermDTO> valores = valoresGermService.obtenerValoresPorTabla(tablaId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    
    @Operation(summary = "Obtener valores de germinación de INIA para una tabla", 
              description = "Obtiene los valores de germinación específicos del instituto INIA para una tabla dada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/inia")
    public ResponseEntity<?> obtenerValoresIniaPorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresIniaPorTabla(tablaId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Obtener valores de germinación de INASE para una tabla", 
              description = "Obtiene los valores de germinación específicos del instituto INASE para una tabla dada")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/inase")
    public ResponseEntity<?> obtenerValoresInasePorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresInasePorTabla(tablaId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Obtener valores de germinación por tabla e instituto", 
              description = "Obtiene los valores de germinación asociados a una tabla específica y un instituto dado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/instituto")
    public ResponseEntity<?> obtenerValoresPorTablaEInstituto(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @RequestParam Instituto instituto) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresPorTablaEInstituto(tablaId, instituto);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    
    @Operation(summary = "Eliminar valores de germinación", 
              description = "Elimina los valores específicos de germinación por su ID")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{valoresId}")
    public ResponseEntity<?> eliminarValores(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long valoresId) {
        try {
            valoresGermService.eliminarValores(valoresId);
            return new ResponseEntity<>("Valores de germinación eliminados exitosamente", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}