package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public record ReservationRequest(
        @NotNull(message = "날짜가 존재하지 않습니다.") LocalDate date,
        @NotNull(message = "예약자 정보가 입력되지 않았습니다.") Member member,
        long timeId,
        long themeId
) {

    public Reservation toReservation(Time time, Theme theme) {
        return new Reservation(member, date, time, theme);
    }

}
