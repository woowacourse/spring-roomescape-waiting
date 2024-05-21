package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;

public record ReservationAdminSaveRequest(@NotNull(message = "멤버를 입력해주세요") Long memberId,
                                          @NotNull(message = "예약 날짜를 입력해주세요.") LocalDate date,
                                          @NotNull(message = "예약 시간을 입력해주세요.") Long timeId,
                                          @NotNull(message = "예약 테마를 입력해주세요.") Long themeId) {

    public Reservation toEntity(ReservationTime reservationTime, Theme theme, Member member) {
        return new Reservation(member, date, reservationTime, theme);
    }
}
