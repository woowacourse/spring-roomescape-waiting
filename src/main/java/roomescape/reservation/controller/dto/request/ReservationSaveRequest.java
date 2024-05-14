package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public record ReservationSaveRequest(
        @NotNull
        LocalDate date,

        @NotNull
        long timeId,

        @NotNull
        long themeId
) {
    public Reservation toEntity(final Member member, final ReservationTime reservationTime, final Theme theme) {
        return Reservation.of(member, date, reservationTime, theme);
    }
}
