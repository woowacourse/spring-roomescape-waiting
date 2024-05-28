package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwait.ReservationWait;
import roomescape.domain.reservationwait.ReservationWaitStatus;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.service.dto.validator.IdPositive;

public record ReservationWaitSaveRequest(@NotNull(message = "예약 날짜를 입력해주세요.") LocalDate date,
                                         @NotNull(message = "예약 시간을 입력해주세요.") @IdPositive Long time,
                                         @NotNull(message = "예약 테마를 입력해주세요.") @IdPositive Long theme) {

    public ReservationWait toEntity(ReservationTime reservationTime, Theme theme, Member member) {
        return new ReservationWait(member, date, reservationTime, theme, ReservationWaitStatus.WAITING);
    }
}
