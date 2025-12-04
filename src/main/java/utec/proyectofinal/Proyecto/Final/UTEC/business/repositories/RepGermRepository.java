package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;

@Repository
public interface RepGermRepository extends JpaRepository<RepGerm, Long> {

    
    @Query("SELECT r FROM RepGerm r WHERE r.tablaGerm.tablaGermID = :tablaGermId")
    List<RepGerm> findByTablaGermId(@Param("tablaGermId") Long tablaGermId);

    
    @Query("SELECT COUNT(r) FROM RepGerm r WHERE r.tablaGerm.tablaGermID = :tablaGermId")
    Long countByTablaGermId(@Param("tablaGermId") Long tablaGermId);

    
    @Query("SELECT r FROM RepGerm r WHERE r.tablaGerm.tablaGermID = :tablaGermId AND r.numRep = :numRep")
    RepGerm findByTablaGermIdAndNumRep(@Param("tablaGermId") Long tablaGermId, @Param("numRep") Integer numRep);

    
    @Query("SELECT r FROM RepGerm r WHERE r.tablaGerm.tablaGermID = :tablaGermId ORDER BY r.numRep ASC")
    List<RepGerm> findByTablaGermIdOrderByNumRep(@Param("tablaGermId") Long tablaGermId);
}