package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface AnalisisRepository extends JpaRepository<Analisis, Long> {
    
    
    @Query("SELECT COUNT(a) FROM Analisis a WHERE CAST(a.fechaFin AS date) = :fecha AND a.estado = :estado")
    long countCompletadosEnFecha(@Param("fecha") LocalDate fecha, @Param("estado") Estado estado);
    
    
    long countByEstado(Estado estado);
}
