package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecieRepository extends JpaRepository<Especie, Long> {
    
    List<Especie> findByActivoTrue();
    
    List<Especie> findByActivoFalse();
    
    List<Especie> findByNombreComunContainingIgnoreCaseAndActivoTrue(String nombreComun);
    
    List<Especie> findByNombreCientificoContainingIgnoreCaseAndActivoTrue(String nombreCientifico);
    
    Optional<Especie> findByNombreComun(String nombreComun);
    
    
    Optional<Especie> findByNombreComunIgnoreCase(String nombreComun);
    
    
    @Query("SELECT e FROM Especie e WHERE LOWER(e.nombreComun) LIKE LOWER(CONCAT('%', :nombreComun, '%')) AND e.activo = true")
    List<Especie> buscarPorNombreComunFlexible(@Param("nombreComun") String nombreComun);
    
    
    @Query("SELECT e FROM Especie e WHERE LOWER(e.nombreComun) LIKE LOWER(CONCAT(:nombreComun, '%')) AND e.activo = true")
    List<Especie> buscarPorNombreComunInicio(@Param("nombreComun") String nombreComun);
    
    
    Page<Especie> findByActivoTrueOrderByNombreComunAsc(Pageable pageable);
    
    Page<Especie> findByActivoFalseOrderByNombreComunAsc(Pageable pageable);
    
    Page<Especie> findAllByOrderByNombreComunAsc(Pageable pageable);
}