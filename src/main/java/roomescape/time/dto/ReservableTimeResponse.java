package roomescape.time.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ReservableTimeResponse(
        long timeId,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {
}
