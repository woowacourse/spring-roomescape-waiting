package roomescape.infrastructure;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationSearchFilter;

public class ReservationSpecifications {
    public static Specification<Reservation> byFilter(ReservationSearchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.themeId() != null) {
                predicates.add(cb.equal(root.get("theme").get("id"), filter.themeId()));
            }
            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
            }
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), filter.dateTo()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
