package utec.proyectofinal.Proyecto.Final.UTEC.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utec.proyectofinal.Proyecto.Final.UTEC.exceptions.InvalidCursorException;

import java.util.Base64;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeysetCursor {
    private String lastFecha;  
    private Long lastId;        
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    
    public String encode() {
        try {
            String json = MAPPER.writeValueAsString(this);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes());
        } catch (JsonProcessingException e) {
            throw new InvalidCursorException("Error al codificar cursor", e);
        }
    }
    
    
    public static KeysetCursor decode(String encoded) {
        if (encoded == null || encoded.trim().isEmpty()) {
            throw new InvalidCursorException("Cursor vacío o nulo");
        }
        
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encoded);
            String json = new String(decoded);
            KeysetCursor cursor = MAPPER.readValue(json, KeysetCursor.class);
            
            
            if (cursor.getLastId() == null) {
                throw new InvalidCursorException("Cursor no contiene lastId válido");
            }
            
            return cursor;
        } catch (IllegalArgumentException e) {
            throw new InvalidCursorException("Cursor no es Base64 válido", e);
        } catch (JsonProcessingException e) {
            throw new InvalidCursorException("Cursor no tiene formato JSON válido", e);
        }
    }
    
    
    @JsonIgnore
    public boolean isValid() {
        return lastId != null;
    }
}
