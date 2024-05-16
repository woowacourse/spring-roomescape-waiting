package roomescape.service.schedule.dto;

import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import roomescape.domain.schedule.ReservationTime;

public record ReservationTimeCreateRequest(
        @DateTimeFormat(pattern = "HH:mm") LocalTime startAt) {
    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
