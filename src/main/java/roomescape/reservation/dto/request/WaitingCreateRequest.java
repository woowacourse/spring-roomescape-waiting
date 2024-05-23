package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;

public record WaitingCreateRequest(
        @NotNull LocalDate date,
        @NotNull Long themeId,
        @NotNull Long timeId) {

    public Waiting toWaiting(final Member member, final Theme theme, final ReservationTime reservationTime) {
        return new Waiting(member, date, theme, reservationTime);
    }
}
