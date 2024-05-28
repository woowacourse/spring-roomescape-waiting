package roomescape.service.schedule.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import roomescape.domain.schedule.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeCreateRequest(
        @NotNull(message = "시간을 입력해주세요.") @DateTimeFormat(pattern = "HH:mm") LocalTime startAt) {
    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
