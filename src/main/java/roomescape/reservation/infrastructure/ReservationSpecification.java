package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.reservation.domain.Reservation;

public class ReservationSpecification {
    public static Specification<Reservation> memberIdEqual(Long memberId) {
        return (root, query, cb) -> cb.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<Reservation> themeIdEqual(Long themeId) {
        return (root, query, cb) -> cb.equal(root.get("theme").get("id"), themeId);
    }

    public static Specification<Reservation> dateEqualOrAfter(LocalDate dateFrom) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    public static Specification<Reservation> dateEqualOrBefore(LocalDate dateTo) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), dateTo);
    }
}
