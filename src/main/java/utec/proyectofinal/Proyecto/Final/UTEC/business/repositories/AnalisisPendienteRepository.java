package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections.AnalisisPendienteProjection;

import java.util.List;

@Repository
public interface AnalisisPendienteRepository extends JpaRepository<Lote, Long> {
    
    
    @Query(value = """
        SELECT DISTINCT 
            l.loteid AS loteid,
            l.nom_lote AS nom_lote,
            l.ficha AS ficha,
            e.nombre_comun AS especieNombre,
            c.nombre AS cultivarNombre,
            lta.tipo_analisis AS tipoAnalisis
        FROM lote l
        INNER JOIN cultivar c ON l.cultivarid = c.cultivarid
        INNER JOIN especie e ON c.especieid = e.especieid
        INNER JOIN lote_tipos_analisis lta ON l.loteid = lta.lote_id
        WHERE l.activo = true
        AND (
            (lta.tipo_analisis = 'PMS' AND NOT EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'GERMINACION' AND NOT EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'DOSN' AND NOT EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'TETRAZOLIO' AND NOT EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'PUREZA' AND NOT EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'PMS' AND EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'GERMINACION' AND EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'DOSN' AND EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'TETRAZOLIO' AND EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'PUREZA' AND EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
        )
        ORDER BY l.loteid, lta.tipo_analisis
        """,
        countQuery = """
        SELECT COUNT(DISTINCT CONCAT(l.loteid, '-', lta.tipo_analisis))
        FROM lote l
        INNER JOIN cultivar c ON l.cultivarid = c.cultivarid
        INNER JOIN especie e ON c.especieid = e.especieid
        INNER JOIN lote_tipos_analisis lta ON l.loteid = lta.lote_id
        WHERE l.activo = true
        AND (
            (lta.tipo_analisis = 'PMS' AND NOT EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'GERMINACION' AND NOT EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'DOSN' AND NOT EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'TETRAZOLIO' AND NOT EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'PUREZA' AND NOT EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'PMS' AND EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'GERMINACION' AND EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'DOSN' AND EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'TETRAZOLIO' AND EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'PUREZA' AND EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
        )
        """,
        nativeQuery = true)
    Page<AnalisisPendienteProjection> findAllPaginado(Pageable pageable);
    
    
    @Query(value = """
        SELECT DISTINCT 
            l.loteid AS loteid,
            l.nom_lote AS nom_lote,
            l.ficha AS ficha,
            e.nombre_comun AS especieNombre,
            c.nombre AS cultivarNombre,
            lta.tipo_analisis AS tipoAnalisis
        FROM lote l
        INNER JOIN cultivar c ON l.cultivarid = c.cultivarid
        INNER JOIN especie e ON c.especieid = e.especieid
        INNER JOIN lote_tipos_analisis lta ON l.loteid = lta.lote_id
        WHERE l.activo = true
        
        AND (l.loteid, lta.tipo_analisis) > (:lastLoteId, :lastTipo)
        AND (
            (lta.tipo_analisis = 'PMS' AND NOT EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'GERMINACION' AND NOT EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'DOSN' AND NOT EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'TETRAZOLIO' AND NOT EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'PUREZA' AND NOT EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ))
            OR (lta.tipo_analisis = 'PMS' AND EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'GERMINACION' AND EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'DOSN' AND EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'TETRAZOLIO' AND EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
            OR (lta.tipo_analisis = 'PUREZA' AND EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true
            ) AND NOT EXISTS (
                SELECT 1 FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid WHERE a.loteid = l.loteid AND a.activo = true AND a.estado != 4
            ))
        )
        ORDER BY l.loteid, lta.tipo_analisis
        LIMIT :size
        """,
        nativeQuery = true)
    List<AnalisisPendienteProjection> findNextPageByCursor(
            Long lastLoteId,
            String lastTipo,
            int size);
}

