package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.domain.Specification;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

public class ReservationSpecs {

    public static Specification<Reservation> isMemberReservation(final UserId userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, builder)
                -> builder.equal(root.get(Reservation.Fields.userId), userId);
    }

    public static Specification<Reservation> isThemeReservation(final ThemeId themeId) {
        if (themeId == null) {
            return null;
        }
        return (root, query, builder)
                -> builder.equal(root.get(Reservation.Fields.theme).get(Theme.Fields.id), themeId.getValue());
    }

    public static Specification<Reservation> isReservationByPeriod(ReservationDate from, ReservationDate to) {
        if (from == null || to == null) {
            return null;
        }
        return (root, query, builder)
                -> builder.between(root.get(Reservation.Fields.date).get(ReservationDate.Fields.value),
                from.getValue(), to.getValue());
    }
}
