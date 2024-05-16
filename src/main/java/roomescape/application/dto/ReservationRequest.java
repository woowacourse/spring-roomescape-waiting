package roomescape.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationRequest(
        @NotNull LocalDate date,
        long timeId,
        long themeId
) {
    public Reservation toReservation(Member member, ReservationTime reservationTime, Theme theme) {
        return new Reservation(member, date, reservationTime, theme);
    }
}
