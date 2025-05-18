package roomescape.domain.reservation;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public record ReservationSearchFilter(
    @Nullable Long themeId,
    @Nullable Long userId,
    @Nullable LocalDate dateFrom,
    @Nullable LocalDate dateTo
) {

    public Specification<Reservation> toSpecification() {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (themeId != null) {
                predicates.add(cb.equal(root.get("theme").get("id"), themeId));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), dateTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
