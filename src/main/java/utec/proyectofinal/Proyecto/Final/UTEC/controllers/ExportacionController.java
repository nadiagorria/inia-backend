package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ExportacionExcelService;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ExportacionRequestDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping("/api/exportaciones")
@Tag(name = "Exportaciones", description = "API para exportar datos de análisis de semillas")
@SecurityRequirement(name = "bearerAuth")
public class ExportacionController {

    @Autowired
    private ExportacionExcelService exportacionExcelService;

    @GetMapping("/excel")
    @Operation(
        summary = "Exportar datos a Excel", 
        description = "Genera un archivo Excel con los datos de análisis de semillas según la estructura de la planilla de ejemplo. Si no se especifican IDs de lotes, se exportan todos los lotes activos."
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<byte[]> exportarDatosExcel(
            @Parameter(description = "Lista de IDs de lotes a exportar. Si está vacío, se exportan todos los lotes activos.")
            @RequestParam(required = false) List<Long> loteIds) {
        
        try {
            byte[] excelBytes = exportacionExcelService.generarReporteExcel(loteIds);
            
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "analisis_semillas_" + timestamp + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            System.err.println("Error al generar archivo Excel: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("Error inesperado al exportar datos: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/excel/lote/{loteId}")
    @Operation(
        summary = "Exportar datos de un lote específico a Excel", 
        description = "Genera un archivo Excel con los datos de análisis de un lote específico"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<byte[]> exportarLoteEspecificoExcel(
            @Parameter(description = "ID del lote a exportar")
            @PathVariable Long loteId) {
        
        try {
            List<Long> loteIds = List.of(loteId);
            byte[] excelBytes = exportacionExcelService.generarReporteExcel(loteIds);
            
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "analisis_lote_" + loteId + "_" + timestamp + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            System.err.println("Error al generar archivo Excel para lote " + loteId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("Error inesperado al exportar lote " + loteId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/excel/personalizado")
    @Operation(
        summary = "Exportar datos personalizados a Excel", 
        description = "Genera un archivo Excel con una lista específica de lotes proporcionada en el cuerpo de la petición"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<byte[]> exportarDatosPersonalizadosExcel(
            @Parameter(description = "Lista de IDs de lotes a exportar")
            @RequestBody List<Long> loteIds) {
        
        try {
            if (loteIds == null || loteIds.isEmpty()) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            
            byte[] excelBytes = exportacionExcelService.generarReporteExcel(loteIds);
            
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "analisis_seleccionados_" + timestamp + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            System.err.println("Error al generar archivo Excel personalizado: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("Error inesperado al exportar datos personalizados: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/excel/avanzado")
    @Operation(
        summary = "Exportar datos con filtros avanzados a Excel", 
        description = "Genera un archivo Excel con filtros avanzados como fechas, especies, cultivares y tipos de análisis"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<byte[]> exportarDatosAvanzadosExcel(
            @Parameter(description = "Configuración de exportación con filtros avanzados")
            @RequestBody ExportacionRequestDTO solicitud) {
        
        try {
            byte[] excelBytes = exportacionExcelService.generarReporteExcelAvanzado(solicitud);
            
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "analisis_filtrado_" + timestamp + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            System.err.println("Error al generar archivo Excel avanzado: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("Error inesperado al exportar datos avanzados: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}