package roomescape.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.reservation.Reservation;

public class ReservationSpecifications {

    public static Specification<Reservation> hasMemberId(final Long memberId) {
        return (root, query, builder) -> {
            if (memberId == null) {
                return null;
            }
            return builder.equal(root.get("member").get("id"), memberId);
        };
    }

    public static Specification<Reservation> hasThemeId(final Long themeId) {
        return (root, query, builder) -> {
            if (themeId == null) {
                return null;
            }
            return builder.equal(root.get("theme").get("id"), themeId);
        };
    }

    public static Specification<Reservation> dateAfterOrEqual(final LocalDate fromDate) {
        return (root, query, builder) -> {
            if (fromDate == null) {
                return null;
            }
            return builder.greaterThanOrEqualTo(root.get("reservationDate").get("date"), fromDate);
        };
    }

    public static Specification<Reservation> dateBeforeOrEqual(final LocalDate toDate) {
        return (root, query, builder) -> {
            if (toDate == null) {
                return null;
            }
            return builder.lessThanOrEqualTo(root.get("reservationDate").get("date"), toDate);
        };
    }
}
