package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.BackupCode;

import java.util.List;


@Repository
public interface BackupCodeRepository extends JpaRepository<BackupCode, Long> {

    
    List<BackupCode> findByUsuarioIdAndUsedFalse(Integer usuarioId);

    
    List<BackupCode> findByUsuarioId(Integer usuarioId);

    
    long countByUsuarioIdAndUsedFalse(Integer usuarioId);

    
    @Modifying
    @Query("DELETE FROM BackupCode b WHERE b.usuarioId = :usuarioId")
    void deleteAllByUsuarioId(Integer usuarioId);

    
    @Modifying
    @Query("DELETE FROM BackupCode b WHERE b.usuarioId = :usuarioId AND b.used = true")
    void deleteUsedByUsuarioId(Integer usuarioId);
}
