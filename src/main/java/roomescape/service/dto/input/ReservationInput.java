package roomescape.service.dto.input;

import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;

public record ReservationInput(String date, Long timeId, Long themeId, Long memberId) {

    public ReservationInfo toReservation(final ReservationTime time, final Theme theme, final Member member) {
        return ReservationInfo.from(date, time, theme, member);
    }
}
