package roomescape.service.dto.input;

import java.time.LocalTime;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.user.Member;

public record WaitingInput(String date, Long timeId, Long themeId, Long memberId) {
    public Waiting toWaiting(final ReservationTime time, final Theme theme, final Member member,
                             final LocalTime startAt) {
        return new Waiting(null, ReservationDate.from(date), time, theme, member, startAt);
    }
}
