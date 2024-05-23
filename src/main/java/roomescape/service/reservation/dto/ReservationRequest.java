package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public record ReservationRequest(
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate date,
        @NotNull(message = "시간 ID를 입력해주세요.")
        Long timeId,
        @NotNull(message = "테마 ID를 입력해주세요.")
        Long themeId,
        @NotNull(message = "멤버 ID를 입력해주세요.")
        Long memberId) {

    public Reservation toEntity(Member member, ReservationTime reservationTime, Theme theme) {
        return new Reservation(member, date, reservationTime, theme);
    }
}
