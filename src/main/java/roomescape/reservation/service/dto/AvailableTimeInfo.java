package roomescape.reservation.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record AvailableTimeInfo(
        long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {
}
