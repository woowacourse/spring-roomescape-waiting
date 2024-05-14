package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public record ReservationCreateRequest(
        @NotNull Long memberId,
        @NotNull LocalDate date,
        @NotNull Long themeId,
        @NotNull Long timeId
) {
    
    public Reservation toReservation(Member member, Theme theme, ReservationTime reservationTime) {
        return new Reservation(member, date, theme, reservationTime);
    }
}
