package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public record ReservationSaveRequest(@NotNull(message = "예약 날짜를 입력해주세요.") LocalDate date,
                                     @NotNull(message = "예약 시간을 입력해주세요.") Long timeId,
                                     @NotNull(message = "예약 테마를 입력해주세요.") Long themeId) {

    public Reservation toEntity(ReservationSaveRequest request, ReservationTime reservationTime,
                                Theme theme, Member member) {
        return new Reservation(member, request.date(), reservationTime, theme);
    }
}
