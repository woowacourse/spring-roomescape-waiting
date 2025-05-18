package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.reservation.domain.reservation.Reservation;

public class ReservationSpecs {

    public static Specification<Reservation> isMemberReservation(final Long id) {
        if (id == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("member").get("id"), id);
    }

    public static Specification<Reservation> isThemeReservation(final Long id) {
        if (id == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("theme").get("id"), id);
    }

    public static Specification<Reservation> isReservationByPeriod(final LocalDate from, final LocalDate to) {
        return (root, query, builder) -> builder.between(root.get("date"), from, to);
    }
}
