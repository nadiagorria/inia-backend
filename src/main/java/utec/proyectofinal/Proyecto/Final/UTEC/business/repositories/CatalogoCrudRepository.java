package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoCrudRepository extends JpaRepository<Catalogo, Long> {
    
    
    List<Catalogo> findByTipoAndActivoTrue(TipoCatalogo tipo);
    
    
    List<Catalogo> findByTipoAndActivoFalse(TipoCatalogo tipo);
    
    
    List<Catalogo> findByTipo(TipoCatalogo tipo);
    
    
    Optional<Catalogo> findByTipoAndValorAndActivoTrue(TipoCatalogo tipo, String valor);
    
    
    List<Catalogo> findByActivoTrue();
    
    
    @Query("SELECT c FROM Catalogo c WHERE c.tipo = :tipo AND c.valor = :valor")
    Optional<Catalogo> findByTipoAndValor(@Param("tipo") TipoCatalogo tipo, @Param("valor") String valor);
    
    
    Page<Catalogo> findByActivoTrueOrderByTipoAscValorAsc(Pageable pageable);
    
    Page<Catalogo> findByActivoFalseOrderByTipoAscValorAsc(Pageable pageable);
    
    Page<Catalogo> findAllByOrderByTipoAscValorAsc(Pageable pageable);
    
    Page<Catalogo> findByTipoAndActivoTrueOrderByValorAsc(TipoCatalogo tipo, Pageable pageable);
    
    Page<Catalogo> findByTipoAndActivoFalseOrderByValorAsc(TipoCatalogo tipo, Pageable pageable);
    
    Page<Catalogo> findByTipoOrderByValorAsc(TipoCatalogo tipo, Pageable pageable);
}