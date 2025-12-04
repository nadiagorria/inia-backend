package utec.proyectofinal.Proyecto.Final.UTEC.business.specifications;

import org.springframework.data.jpa.domain.Specification;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class LoteSpecification {

    
    public static Specification<Lote> conFiltros(String searchTerm, Boolean activo, String cultivarNombre) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro de búsqueda: busca en ficha, nomLote, cultivar.nombre, especie.nombreCientifico, especie.nombreComun
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate fichaPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("ficha")), searchPattern);
                Predicate nombrePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nomLote")), searchPattern);
                Predicate cultivarPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("cultivar").get("nombre")), searchPattern);
                Predicate especieCientificoPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("cultivar").get("especie").get("nombreCientifico")), searchPattern);
                Predicate especieComunPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("cultivar").get("especie").get("nombreComun")), searchPattern);
                
                predicates.add(criteriaBuilder.or(
                    fichaPredicate, nombrePredicate, cultivarPredicate, especieCientificoPredicate, especieComunPredicate
                ));
            }

            // Filtro por estado activo
            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            // Filtro por nombre de cultivar
            if (cultivarNombre != null && !cultivarNombre.trim().isEmpty() && !"todos".equals(cultivarNombre)) {
                predicates.add(criteriaBuilder.equal(root.get("cultivar").get("nombre"), cultivarNombre));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
