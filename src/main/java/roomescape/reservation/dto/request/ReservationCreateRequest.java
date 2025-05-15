package roomescape.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record ReservationCreateRequest(
    @NotNull(message = "예약 날짜는 빈 값이 올 수 없습니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,
    @NotNull(message = "예약 시간이 올바르지 않습니다")
    Long timeId,
    @NotNull(message = "예약 테마가 올바르지 않습니다")
    Long themeId
) {
    public Reservation toReservation(ReservationTime time, Theme theme, Member member) {
        return new Reservation(null, date, time, theme, member);
    }
}
