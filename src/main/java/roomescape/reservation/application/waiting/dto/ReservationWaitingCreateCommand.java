package roomescape.reservation.application.waiting.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.time.ReservationTime;
import roomescape.reservation.domain.waiting.ReservationWaiting;

public record ReservationWaitingCreateCommand(LocalDate date, long memberId, long timeId, long themeId) {

    public ReservationWaiting convertToEntity(final LocalDate date, final ReservationTime time, final Theme theme, final Member member) {
        return new ReservationWaiting(date, time, theme, member);
    }
}
