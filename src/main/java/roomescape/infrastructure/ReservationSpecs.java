package roomescape.infrastructure;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSearchFilter;

public class ReservationSpecs {

    public static Specification<Reservation> byDate(final LocalDate date) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("slot").get("dateTime").get("date"), date);
    }

    public static Specification<Reservation> byDateBetween(final LocalDate from, final LocalDate to) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.between(root.get("slot").get("dateTime").get("date"), from, to);
    }

    public static Specification<Reservation> byTimeSlotId(final long id) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("slot").get("dateTime").get("timeSlot").get("id"), id);
    }

    public static Specification<Reservation> byThemeId(final long id) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("slot").get("theme").get("id"), id);
    }

    public static Specification<Reservation> byFilter(final ReservationSearchFilter filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (filter.themeId() != null) {
                predicates.add(cb.equal(root.get("slot").get("theme").get("id"), filter.themeId()));
            }
            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
            }
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("slot").get("dateTime").get("date"), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("slot").get("dateTime").get("date"), filter.dateTo()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
