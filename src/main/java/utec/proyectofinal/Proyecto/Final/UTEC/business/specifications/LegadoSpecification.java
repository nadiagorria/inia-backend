package utec.proyectofinal.Proyecto.Final.UTEC.business.specifications;

import org.springframework.data.jpa.domain.Specification;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Legado;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class LegadoSpecification {

    public static Specification<Legado> conFiltros(
            String searchTerm, 
            String especie, 
            LocalDate fechaReciboInicio,
            LocalDate fechaReciboFin) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre filtrar solo activos
            predicates.add(criteriaBuilder.equal(root.get("activo"), true));

            // Filtro de búsqueda por ID o ficha
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                
                // Buscar por ID parcial
                Predicate idPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(criteriaBuilder.toString(root.get("legadoID"))), searchPattern);
                
                // Buscar por ficha del lote
                Predicate fichaPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lote").get("ficha")), searchPattern);
                
                predicates.add(criteriaBuilder.or(idPredicate, fichaPredicate));
            }

            // Filtro por especie (a través del cultivar del lote)
            if (especie != null && !especie.trim().isEmpty() && !"todos".equalsIgnoreCase(especie)) {
                predicates.add(criteriaBuilder.equal(
                    root.get("lote").get("cultivar").get("nombre"), especie));
            }

            // Filtro por rango de fecha de recibo
            if (fechaReciboInicio != null && fechaReciboFin != null) {
                predicates.add(criteriaBuilder.between(
                    root.get("lote").get("fechaRecibo"), fechaReciboInicio, fechaReciboFin));
            } else if (fechaReciboInicio != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("lote").get("fechaRecibo"), fechaReciboInicio));
            } else if (fechaReciboFin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("lote").get("fechaRecibo"), fechaReciboFin));
            }

            // Ordenar por ID descendente
            query.orderBy(criteriaBuilder.desc(root.get("legadoID")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
