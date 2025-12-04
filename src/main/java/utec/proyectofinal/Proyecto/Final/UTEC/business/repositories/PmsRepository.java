package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.util.List;

public interface PmsRepository extends JpaRepository<Pms, Long>, JpaSpecificationExecutor<Pms> {
    List<Pms> findByEstadoNot(Estado estado);
    List<Pms> findByEstado(Estado estado);
    
    
    List<Pms> findByActivoTrue();

    @Query("SELECT p FROM Pms p WHERE p.lote.loteID = :idLote")
    List<Pms> findByIdLote(@Param("idLote") Integer idLote);
    
    List<Pms> findByLoteLoteID(Long loteID);
    
    
    boolean existsByLoteLoteID(Long loteID);
    boolean existsByLoteLoteIDAndEstado(Long loteID, Estado estado);

    
    Page<Pms> findByEstadoNotOrderByFechaInicioDesc(Estado estado, Pageable pageable);
    
    
    Page<Pms> findByActivoTrueOrderByFechaInicioDesc(Pageable pageable);
    Page<Pms> findByActivoFalseOrderByFechaInicioDesc(Pageable pageable);
    Page<Pms> findAllByOrderByFechaInicioDesc(Pageable pageable);

}
