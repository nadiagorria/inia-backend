package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

public interface ValoresGermRepository extends JpaRepository<ValoresGerm, Long> {
    
    
    @Query("SELECT v FROM ValoresGerm v WHERE v.tablaGerm.tablaGermID = :tablaGermId")
    List<ValoresGerm> findByTablaGermId(@Param("tablaGermId") Long tablaGermId);
    
    
    @Query("SELECT v FROM ValoresGerm v WHERE v.tablaGerm.tablaGermID = :tablaGermId AND v.instituto = :instituto")
    Optional<ValoresGerm> findByTablaGermIdAndInstituto(@Param("tablaGermId") Long tablaGermId, @Param("instituto") Instituto instituto);
}
