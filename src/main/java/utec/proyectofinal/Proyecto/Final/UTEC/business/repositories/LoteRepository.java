package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long>, JpaSpecificationExecutor<Lote> {
    
    List<Lote> findByActivoTrue();
    
    List<Lote> findByActivoFalse();
    
    List<Lote> findByActivo(Boolean activo);
    
    @Query("SELECT l FROM Lote l WHERE l.activo = :activo")
    List<Lote> findLotesByActivo(Boolean activo);

    
    Page<Lote> findByActivo(Boolean activo, Pageable pageable);
    
    
    Optional<Lote> findByFicha(String ficha);
    
    
    @Query("SELECT COUNT(l) FROM Lote l WHERE l.activo = true")
    long countLotesActivos();
    
    
    @Query("SELECT COUNT(l) FROM Lote l WHERE l.activo = false")
    long countLotesInactivos();

    Optional<Lote> findByNomLote(String nomLote);
    
    
    @Query("SELECT l FROM Lote l " +
           "LEFT JOIN FETCH l.cultivar c " +
           "LEFT JOIN FETCH c.especie " +
           "WHERE l.loteID = :id")
    Optional<Lote> findByIdWithCultivarAndEspecie(Long id);
}