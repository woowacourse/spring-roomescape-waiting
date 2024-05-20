package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public record MemberReservationAddRequest(
        @NotNull(message = "예약 날짜는 필수 입니다.") LocalDate date,
        @NotNull(message = "예약 시간 선택은 필수 입니다.") @Positive Long timeId,
        @NotNull(message = "테마 선택은 필수 입니다.") @Positive Long themeId) {

    public Reservation toReservation(Member member, ReservationTime reservationTime, Theme theme) {
        return new Reservation(null, member, date, reservationTime, theme);
    }
}
