package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.service.dto.validator.IdPositive;

public record ReservationAdminSaveRequest(@NotNull(message = "멤버를 입력해주세요") Long memberId,
                                          @NotNull(message = "예약 날짜를 입력해주세요.") LocalDate date,
                                          @NotNull(message = "예약 시간을 입력해주세요.") @IdPositive Long timeId,
                                          @NotNull(message = "예약 테마를 입력해주세요.") @IdPositive Long themeId) {

    public Reservation toEntity(ReservationTime reservationTime, Theme theme, Member member) {
        return new Reservation(member, date, reservationTime, theme);
    }
}
