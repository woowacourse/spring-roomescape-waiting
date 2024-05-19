package roomescape.service.schedule.dto;

import org.springframework.format.annotation.DateTimeFormat;
import roomescape.domain.schedule.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeCreateRequest(
        @DateTimeFormat(pattern = "HH:mm") LocalTime startAt) {
    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
