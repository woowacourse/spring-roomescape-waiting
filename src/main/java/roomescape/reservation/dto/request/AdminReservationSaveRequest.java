package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

import java.time.LocalDate;

public record AdminReservationSaveRequest(
        @NotNull(message = "예약 날짜는 비어있을 수 없습니다.")
        LocalDate date,
        @NotNull(message = "예약 시간 Id는 비어있을 수 없습니다.")
        Long timeId,
        @NotNull(message = "테마 Id는 비어있을 수 없습니다.")
        Long themeId,
        @NotNull(message = "사용자 Id는 비어있을 수 없습니다.")
        Long memberId) {

    public Reservation toModel(Theme theme, ReservationTime time, Member member) {
        return new Reservation(member, date, time, theme);
    }
}
