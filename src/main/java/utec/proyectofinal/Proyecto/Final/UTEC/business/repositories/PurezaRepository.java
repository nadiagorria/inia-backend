package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

public interface PurezaRepository extends JpaRepository<Pureza, Long>, JpaSpecificationExecutor<Pureza> {
    
    List<Pureza> findByEstadoNot(Estado estado);
    
    List<Pureza> findByEstado(Estado estado);
    
    
    List<Pureza> findByActivoTrue();
    
    @Query("SELECT p FROM Pureza p WHERE p.lote.loteID = :idLote")
    List<Pureza> findByIdLote(@Param("idLote") Long idLote);
    
    List<Pureza> findByLoteLoteID(Long loteID);

    
    Page<Pureza> findByEstadoNotOrderByFechaInicioDesc(Estado estado, Pageable pageable);
    
    
    Page<Pureza> findByActivoTrueOrderByFechaInicioDesc(Pageable pageable);
    Page<Pureza> findByActivoFalseOrderByFechaInicioDesc(Pageable pageable);
    Page<Pureza> findAllByOrderByFechaInicioDesc(Pageable pageable);
    
    
    boolean existsByLoteLoteID(Long loteID);
    boolean existsByLoteLoteIDAndEstado(Long loteID, Estado estado);

}
