package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeRequest(
        @NotNull(message = "예약 시간(startAt)은 null일 수 없습니다.")
        @DateTimeFormat(pattern = "kk:mm")
        LocalTime startAt
) {

    public ReservationTime toTime() {
        return new ReservationTime(this.startAt);
    }
}
