package utec.proyectofinal.Proyecto.Final.UTEC.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.ErrorResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.exceptions.BadRequestException;
import utec.proyectofinal.Proyecto.Final.UTEC.exceptions.ConflictException;
import utec.proyectofinal.Proyecto.Final.UTEC.exceptions.NotFoundException;

/**
 * Manejador global de excepciones para todos los controladores REST
 * Proporciona respuestas de error consistentes en toda la API
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja BadRequestException - validaciones de negocio fallidas
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    /**
     * Maneja NotFoundException - recurso no encontrado
     * HTTP 404 Not Found
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }

    /**
     * Maneja ConflictException - conflictos de estado o recursos duplicados
     * HTTP 409 Conflict
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.CONFLICT.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(error);
    }

    /**
     * Maneja excepciones de acceso denegado (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            "No tienes permisos para realizar esta operación",
            HttpStatus.FORBIDDEN.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(error);
    }

    /**
     * Maneja IllegalStateException - errores de estado interno del sistema
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    /**
     * Maneja IllegalArgumentException (argumentos inválidos)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    /**
     * Maneja excepciones de RuntimeException lanzadas por la lógica de negocio
     * Si el mensaje contiene palabras clave de "no encontrado", retorna 404
     * De lo contrario, retorna 400 Bad Request
     * Nota: IllegalStateException se maneja en su propio handler específico
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, 
            HttpServletRequest request) {
        
        
        if (ex instanceof IllegalStateException) {
            throw ex; 
        }
        
        String mensaje = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        
        if (mensaje != null && (mensaje.toLowerCase().contains("no encontrad") || 
                                mensaje.toLowerCase().contains("not found"))) {
            status = HttpStatus.NOT_FOUND;
        }
        
        ErrorResponse error = new ErrorResponse(
            mensaje,
            status.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(status)
            .body(error);
    }

    /**
     * Maneja cualquier otra excepción no capturada (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            "Error interno del servidor: " + ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
}
