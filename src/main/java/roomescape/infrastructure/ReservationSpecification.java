package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;

public class ReservationSpecification {
    public static Specification<Reservation> memberIdEqual(Optional<Long> memberId) {
        return (root, query, cb) -> {
            if (memberId.isEmpty()) {
                return null;
            }
            return cb.equal(root.get("member").get("id"), memberId.get());
        };
    }

    public static Specification<Reservation> themeIdEqual(Optional<Long> themeId) {
        return (root, query, cb) -> {
            if (themeId.isEmpty()) {
                return null;
            }
            return cb.equal(root.get("theme").get("id"), themeId.get());
        };
    }

    public static Specification<Reservation> dateBetween(Optional<LocalDate> dateFrom, Optional<LocalDate> dateTo) {
        return (root, query, cb) -> {
            if (dateFrom.isEmpty() || dateTo.isEmpty()) {
                return null;
            }
            return cb.between(root.get("date"), dateFrom.get(), dateTo.get());
        };
    }
}
