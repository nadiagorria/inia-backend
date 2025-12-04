package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.util.List;

public interface DosnRepository extends JpaRepository<Dosn, Long>, JpaSpecificationExecutor<Dosn> {

    List<Dosn> findByEstadoNot(Estado estado);
    List<Dosn> findByEstado(Estado estado);
    
    
    List<Dosn> findByActivoTrue();

    @Query("SELECT d FROM Dosn d WHERE d.lote.loteID = :idLote")
    List<Dosn> findByIdLote(@Param("idLote") Integer idLote);
    
    List<Dosn> findByLoteLoteID(Long loteID);
    
    
    boolean existsByLoteLoteID(Long loteID);
    boolean existsByLoteLoteIDAndEstado(Long loteID, Estado estado);

    
    Page<Dosn> findByEstadoNotOrderByFechaInicioDesc(Estado estado, Pageable pageable);
    
    
    Page<Dosn> findByActivoTrueOrderByFechaInicioDesc(Pageable pageable);
    Page<Dosn> findByActivoFalseOrderByFechaInicioDesc(Pageable pageable);
    Page<Dosn> findAllByOrderByFechaInicioDesc(Pageable pageable);

}
