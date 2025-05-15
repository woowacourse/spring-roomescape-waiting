package roomescape.dto;

import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

public record ReservationTimeRequest(
        @DateTimeFormat(pattern = "HH:mm")
        LocalTime startAt
) {
}
