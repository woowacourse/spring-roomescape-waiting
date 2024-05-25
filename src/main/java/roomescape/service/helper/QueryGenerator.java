package roomescape.service.helper;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.reservation.Reservation;
import roomescape.service.dto.reservation.ReservationSearchParams;

public class QueryGenerator {

    public static Specification<Reservation> getSearchSpecification(ReservationSearchParams request) {
        return ((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            root.fetch("member");
            root.fetch("theme");
            root.fetch("time");

            if (request.getStatus() != null) {
                predicates.add(builder.equal(root.get("status"), request.getStatus()));
            }
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                predicates.add(builder.equal(root.get("member").get("email"), request.getEmail()));
            }
            if (request.getThemeId() != null) {
                predicates.add(builder.equal(root.get("theme").get("id"), request.getThemeId()));
            }
            if (request.getStartDate() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), request.getEndDate()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
