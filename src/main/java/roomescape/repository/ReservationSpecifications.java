package roomescape.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.reservation.Reservation;

public class ReservationSpecifications {

    public static Specification<Reservation> hasThemeId(final Long themeId) {
        return (root, query, builder) -> themeId == null ? null : builder.equal(root.get("theme").get("id"), themeId);
    }

    public static Specification<Reservation> hasMemberId(final Long memberId) {
        return (root, query, builder) -> memberId == null ? null
                : builder.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<Reservation> dateAfterOrEqual(final LocalDate from) {
        return (root, query, builder) ->
                from == null ? null : builder.greaterThanOrEqualTo(root.get("reservationDate").get("date"), from);
    }

    public static Specification<Reservation> dateBeforeOrEqual(final LocalDate to) {
        return (root, query, builder) ->
                to == null ? null : builder.lessThanOrEqualTo(root.get("reservationDate").get("date"), to);
    }
}
