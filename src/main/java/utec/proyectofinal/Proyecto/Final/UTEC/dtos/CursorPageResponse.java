package utec.proyectofinal.Proyecto.Final.UTEC.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> items;
    private String nextCursor;  
    private boolean hasMore;
    private int size;
    
    
    public CursorPageResponse(List<T> items, int size) {
        this.items = items;
        this.nextCursor = null;
        this.hasMore = false;
        this.size = size;
    }
    
    
    public static <T> CursorPageResponse<T> of(List<T> items, KeysetCursor nextCursor, int size) {
        String encodedCursor = nextCursor != null ? nextCursor.encode() : null;
        return new CursorPageResponse<>(items, encodedCursor, true, size);
    }
    
    
    public static <T> CursorPageResponse<T> lastPage(List<T> items, int size) {
        return new CursorPageResponse<>(items, null, false, size);
    }
}
