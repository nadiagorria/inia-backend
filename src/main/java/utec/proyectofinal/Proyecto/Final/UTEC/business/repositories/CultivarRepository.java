package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;

import java.util.List;
import java.util.Optional;

@Repository
public interface CultivarRepository extends JpaRepository<Cultivar, Long> {
    
    List<Cultivar> findByActivoTrue();
    
    List<Cultivar> findByActivoFalse();
    
    List<Cultivar> findByEspecieAndActivoTrue(Especie especie);
    
    List<Cultivar> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    
    List<Cultivar> findByEspecieEspecieIDAndActivoTrue(Long especieID);
    
    Optional<Cultivar> findByNombreAndEspecie_EspecieID(String nombre, Long especieID);
    
    
    Page<Cultivar> findByActivoTrueOrderByNombreAsc(Pageable pageable);
    
    Page<Cultivar> findByActivoFalseOrderByNombreAsc(Pageable pageable);
    
    Page<Cultivar> findAllByOrderByNombreAsc(Pageable pageable);
}