package utec.proyectofinal.Proyecto.Final.UTEC.business.specifications;

import org.springframework.data.jpa.domain.Specification;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PmsSpecification {

    public static Specification<Pms> conFiltros(String searchTerm, Boolean activo, String estado, Long loteId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                
                
                Predicate idPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(criteriaBuilder.toString(root.get("analisisID"))), searchPattern);
                
                Predicate fichaPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lote").get("ficha")), searchPattern);
                
                Predicate nomLotePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lote").get("nomLote")), searchPattern);
                
                predicates.add(criteriaBuilder.or(idPredicate, fichaPredicate, nomLotePredicate));
            }

            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            if (estado != null && !estado.trim().isEmpty() && !"todos".equalsIgnoreCase(estado)) {
                try {
                    Estado estadoEnum = Estado.valueOf(estado.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("estado"), estadoEnum));
                } catch (IllegalArgumentException e) {
                    
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
