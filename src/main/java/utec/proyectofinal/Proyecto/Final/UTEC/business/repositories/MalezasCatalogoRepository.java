package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasCatalogo;

import java.util.List;
import java.util.Optional;

@Repository
public interface MalezasCatalogoRepository extends JpaRepository<MalezasCatalogo, Long> {
    
    List<MalezasCatalogo> findByActivoTrue();
    
    List<MalezasCatalogo> findByActivoFalse();
    
    List<MalezasCatalogo> findByNombreComunContainingIgnoreCaseAndActivoTrue(String nombreComun);
    
    List<MalezasCatalogo> findByNombreCientificoContainingIgnoreCaseAndActivoTrue(String nombreCientifico);
    
    
    Optional<MalezasCatalogo> findByNombreComunIgnoreCase(String nombreComun);
    
    Optional<MalezasCatalogo> findByNombreCientificoIgnoreCase(String nombreCientifico);
    
    /**
     * Busca una maleza por nombre común con coincidencia flexible:
     * - Ignora mayúsculas/minúsculas
     * - El nombre de búsqueda puede ser parte del nombre almacenado
     */
    @Query("SELECT m FROM MalezasCatalogo m WHERE LOWER(m.nombreComun) LIKE LOWER(CONCAT('%', :nombreComun, '%')) AND m.activo = true")
    List<MalezasCatalogo> buscarPorNombreComunFlexible(@Param("nombreComun") String nombreComun);
    
    /**
     * Busca una maleza donde el nombre común almacenado comience con el texto proporcionado
     */
    @Query("SELECT m FROM MalezasCatalogo m WHERE LOWER(m.nombreComun) LIKE LOWER(CONCAT(:nombreComun, '%')) AND m.activo = true")
    List<MalezasCatalogo> buscarPorNombreComunInicio(@Param("nombreComun") String nombreComun);
    
    
    Page<MalezasCatalogo> findByActivoTrueOrderByNombreComunAsc(Pageable pageable);
    
    Page<MalezasCatalogo> findByActivoFalseOrderByNombreComunAsc(Pageable pageable);
    
    Page<MalezasCatalogo> findAllByOrderByNombreComunAsc(Pageable pageable);
}