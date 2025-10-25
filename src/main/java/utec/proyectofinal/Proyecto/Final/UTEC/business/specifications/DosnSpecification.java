package utec.proyectofinal.Proyecto.Final.UTEC.business.specifications;

import org.springframework.data.jpa.domain.Specification;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class DosnSpecification {

    public static Specification<Dosn> conFiltros(String searchTerm, Boolean activo, String estado, Long loteId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                
                Predicate idPredicate = null;
                try {
                    Long id = Long.parseLong(searchTerm.trim());
                    idPredicate = criteriaBuilder.equal(root.get("analisisID"), id);
                } catch (NumberFormatException e) {
                    // Ignorar
                }
                
                Predicate fichaPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lote").get("ficha")), searchPattern);
                
                Predicate nomLotePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lote").get("nomLote")), searchPattern);
                
                if (idPredicate != null) {
                    predicates.add(criteriaBuilder.or(idPredicate, fichaPredicate, nomLotePredicate));
                } else {
                    predicates.add(criteriaBuilder.or(fichaPredicate, nomLotePredicate));
                }
            }

            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            if (estado != null && !estado.trim().isEmpty() && !"todos".equalsIgnoreCase(estado)) {
                try {
                    Estado estadoEnum = Estado.valueOf(estado.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("estado"), estadoEnum));
                } catch (IllegalArgumentException e) {
                    // Ignorar
                }
            }

            if (loteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("lote").get("loteID"), loteId));
            }

            query.orderBy(criteriaBuilder.desc(root.get("fechaInicio")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
