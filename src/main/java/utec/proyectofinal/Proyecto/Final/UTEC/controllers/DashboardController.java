package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPendienteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPorAprobarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.DashboardStatsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.CursorPageResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.exceptions.InvalidCursorException;
import utec.proyectofinal.Proyecto.Final.UTEC.services.DashboardService;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> obtenerEstadisticas() {
        try {
            DashboardStatsDTO stats = dashboardService.obtenerEstadisticas();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas del dashboard: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint offset estándar para análisis pendientes.
     * Usa paginación con page/size para navegación de páginas.
     * 
     * @param page Número de página (base 0)
     * @param size Número de items por página (default 10)
     * @return Página de análisis pendientes
     */
    @GetMapping("/analisis-pendientes")
    public ResponseEntity<Page<AnalisisPendienteDTO>> obtenerAnalisisPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AnalisisPendienteDTO> response = dashboardService.listarAnalisisPendientesPaginados(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al obtener análisis pendientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint offset estándar para análisis por aprobar.
     * Usa paginación con page/size para navegación de páginas.
     * 
     * @param page Número de página (base 0)
     * @param size Número de items por página (default 10)
     * @return Página de análisis por aprobar
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/analisis-por-aprobar")
    public ResponseEntity<Page<AnalisisPorAprobarDTO>> obtenerAnalisisPorAprobar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AnalisisPorAprobarDTO> response = dashboardService.listarAnalisisPorAprobarPaginados(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al obtener análisis por aprobar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint keyset para análisis pendientes.
     * Usa cursor Base64 para paginación eficiente sin OFFSET.
     * 
     * @param cursor Base64-encoded cursor de la última página (opcional para primera página)
     * @param size Número de items por página (default 20)
     * @return Página con items y nextCursor
     */
    @GetMapping("/analisis-pendientes/keyset")
    public ResponseEntity<?> obtenerAnalisisPendientesKeyset(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        try {
            CursorPageResponse<AnalisisPendienteDTO> response = 
                dashboardService.listarAnalisisPendientesKeyset(cursor, size);
            return ResponseEntity.ok(response);
        } catch (InvalidCursorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Cursor inválido: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error keyset analisis pendientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint keyset para análisis por aprobar.
     * Usa cursor Base64 para paginación eficiente sin OFFSET.
     * 
     * @param cursor Base64-encoded cursor de la última página (opcional para primera página)
     * @param size Número de items por página (default 20)
     * @return Página con items y nextCursor
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/analisis-por-aprobar/keyset")
    public ResponseEntity<?> obtenerAnalisisPorAprobarKeyset(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        try {
            CursorPageResponse<AnalisisPorAprobarDTO> response = 
                dashboardService.listarAnalisisPorAprobarKeyset(cursor, size);
            return ResponseEntity.ok(response);
        } catch (InvalidCursorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Cursor inválido: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error keyset analisis por aprobar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * DTO simple para respuestas de error.
     */
    private static class ErrorResponse {
        public final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
