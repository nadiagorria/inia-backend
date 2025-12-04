package utec.proyectofinal.Proyecto.Final.UTEC.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Respuesta genérica para paginación basada en cursor (keyset).
 * Evita el overhead de COUNT y permite scroll infinito eficiente.
 * 
 * El cursor se devuelve como String Base64 para seguridad y portabilidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> items;
    private String nextCursor;  
    private boolean hasMore;
    private int size;
    
    /**
     * Constructor para página sin más resultados
     */
    public CursorPageResponse(List<T> items, int size) {
        this.items = items;
        this.nextCursor = null;
        this.hasMore = false;
        this.size = size;
    }
    
    /**
     * Constructor para página con más resultados.
     * Codifica el cursor automáticamente.
     */
    public static <T> CursorPageResponse<T> of(List<T> items, KeysetCursor nextCursor, int size) {
        String encodedCursor = nextCursor != null ? nextCursor.encode() : null;
        return new CursorPageResponse<>(items, encodedCursor, true, size);
    }
    
    /**
     * Constructor para última página
     */
    public static <T> CursorPageResponse<T> lastPage(List<T> items, int size) {
        return new CursorPageResponse<>(items, null, false, size);
    }
}
