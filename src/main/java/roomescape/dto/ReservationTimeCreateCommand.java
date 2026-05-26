package roomescape.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

public record ReservationTimeCreateCommand(
        @DateTimeFormat(pattern = "hh:mm")
        @NotNull(message = "START_TIME_NULL")
        LocalTime startAt,

        @DateTimeFormat(pattern = "hh:mm")
        @NotNull(message = "END_TIME_NULL")
        LocalTime endAt
) {
}
