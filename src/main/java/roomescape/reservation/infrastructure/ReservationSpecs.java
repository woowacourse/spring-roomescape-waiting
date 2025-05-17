package roomescape.reservation.infrastructure;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

public class ReservationSpecs {

    public static Specification<Reservation> isMemberReservation(final UserId userId) {
        return (userId == null) ? null : (root, query, builder)
                -> builder.equal(root.get("userId"), userId.getValue());
    }

    public static Specification<Reservation> isThemeReservation(final ThemeId themeId) {
        return (themeId == null) ? null : (root, query, builder)
                -> builder.equal(root.get("theme").get("id"), themeId.getValue());
    }

    public static Specification<Reservation> isReservationByPeriod(final ReservationDate from, final ReservationDate to) {
        return (from == null && to == null) ? null : (root, query, builder) -> {
            Predicate predicate = null;

            if (from != null) {
                predicate = builder.greaterThanOrEqualTo(root.get("date").get("value"), from.getValue());
            }

            if (to != null) {
                Predicate toCondition = builder.lessThanOrEqualTo(root.get("date").get("value"), to.getValue());
                predicate = (predicate == null) ? toCondition : builder.and(predicate, toCondition);
            }

            return predicate;
        };
    }
}
