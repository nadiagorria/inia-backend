package utec.proyectofinal.Proyecto.Final.UTEC.business.specifications;

import org.springframework.data.jpa.domain.Specification;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PurezaSpecification {

    
    public static Specification<Pureza> conFiltros(String searchTerm, Boolean activo, String estado, Long loteId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro de búsqueda: busca en ID de análisis y ficha del lote
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                
                // Buscar por ID parcial
                Predicate idPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(criteriaBuilder.toString(root.get("analisisID"))), searchPattern);
                
                // Buscar por ficha del lote
                Predicate fichaPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lote").get("ficha")), searchPattern);
                
                // Buscar por nombre del lote
                Predicate nomLotePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lote").get("nomLote")), searchPattern);
                
                predicates.add(criteriaBuilder.or(idPredicate, fichaPredicate, nomLotePredicate));
            }

            // Filtro por estado activo
            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            // Filtro por estado del análisis
            if (estado != null && !estado.trim().isEmpty() && !"todos".equalsIgnoreCase(estado)) {
                try {
                    Estado estadoEnum = Estado.valueOf(estado.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("estado"), estadoEnum));
                } catch (IllegalArgumentException e) {
                    // Estado inválido, ignorar
                }
            }

            // Filtro por lote
            if (loteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("lote").get("loteID"), loteId));
            }

            // Ordenar por fecha de inicio descendente
            query.orderBy(criteriaBuilder.desc(root.get("fechaInicio")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
