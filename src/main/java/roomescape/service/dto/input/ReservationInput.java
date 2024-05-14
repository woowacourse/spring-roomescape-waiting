package roomescape.service.dto.input;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;

public record ReservationInput(String date, Long timeId, Long themeId, Long memberId) {

    public Reservation toReservation(final ReservationTime time, final Theme theme, final Member member) {
        return Reservation.from(null, date, time, theme, member);
    }
}
