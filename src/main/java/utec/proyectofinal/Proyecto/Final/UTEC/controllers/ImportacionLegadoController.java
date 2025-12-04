package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ImportacionLegadoResponseDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ImportacionLegadoService;

/**
 * Controlador para la importación de datos legados desde Excel
 */

@RestController
@RequestMapping("/api/importacion")
@Tag(name = "Importación Legado", description = "API para importar datos históricos desde Excel")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class ImportacionLegadoController {

    private final ImportacionLegadoService importacionService;

    /**
     * Endpoint para validar un archivo Excel sin importar los datos
     * 
     * @param archivo Archivo Excel a validar
     * @return Resultado de la validación con errores detectados
     */
    @PostMapping(value = "/legado/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Validar archivo Excel", 
        description = "Valida la estructura y datos del archivo Excel sin importarlos a la base de datos"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<ImportacionLegadoResponseDTO> validarArchivo(
            @RequestParam("archivo") MultipartFile archivo) {
        
        log.info("Iniciando validación de archivo: {}", archivo.getOriginalFilename());
        
        try {
            
            if (archivo.isEmpty()) {
                ImportacionLegadoResponseDTO response = new ImportacionLegadoResponseDTO();
                response.setExitoso(false);
                response.setMensaje("El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }
            
            
            String nombreArchivo = archivo.getOriginalFilename();
            if (nombreArchivo == null || 
                (!nombreArchivo.endsWith(".xlsx") && !nombreArchivo.endsWith(".xls"))) {
                ImportacionLegadoResponseDTO response = new ImportacionLegadoResponseDTO();
                response.setExitoso(false);
                response.setMensaje("El archivo debe ser un Excel (.xlsx o .xls)");
                return ResponseEntity.badRequest().body(response);
            }
            
            
            ImportacionLegadoResponseDTO resultado = importacionService.importarDesdeExcel(archivo, true);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error al validar archivo: {}", e.getMessage(), e);
            
            ImportacionLegadoResponseDTO response = new ImportacionLegadoResponseDTO();
            response.setExitoso(false);
            response.setMensaje("Error al validar el archivo: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para importar datos desde un archivo Excel
     * 
     * @param archivo Archivo Excel con los datos a importar
     * @return Resultado de la importación
     */
    @PostMapping(value = "/legado/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Importar datos desde Excel", 
        description = "Importa datos históricos desde un archivo Excel a la base de datos"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportacionLegadoResponseDTO> importarArchivo(
            @RequestParam("archivo") MultipartFile archivo) {
        
        log.info("Iniciando importación de archivo: {}", archivo.getOriginalFilename());
        
        try {
            
            if (archivo.isEmpty()) {
                ImportacionLegadoResponseDTO response = new ImportacionLegadoResponseDTO();
                response.setExitoso(false);
                response.setMensaje("El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }
            
            
            String nombreArchivo = archivo.getOriginalFilename();
            if (nombreArchivo == null || 
                (!nombreArchivo.endsWith(".xlsx") && !nombreArchivo.endsWith(".xls"))) {
                ImportacionLegadoResponseDTO response = new ImportacionLegadoResponseDTO();
                response.setExitoso(false);
                response.setMensaje("El archivo debe ser un Excel (.xlsx o .xls)");
                return ResponseEntity.badRequest().body(response);
            }
            
            
            ImportacionLegadoResponseDTO resultado = importacionService.importarDesdeExcel(archivo, false);
            
            if (resultado.getExitoso()) {
                log.info("Importación completada exitosamente: {} filas importadas", 
                         resultado.getFilasImportadas());
                return ResponseEntity.ok(resultado);
            } else {
                log.warn("Importación completada con errores: {} filas con errores", 
                         resultado.getFilasConErrores());
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(resultado);
            }
            
        } catch (Exception e) {
            log.error("Error al importar archivo: {}", e.getMessage(), e);
            
            ImportacionLegadoResponseDTO response = new ImportacionLegadoResponseDTO();
            response.setExitoso(false);
            response.setMensaje("Error al importar el archivo: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
