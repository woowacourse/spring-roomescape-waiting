package roomescape.service.dto;

import java.time.LocalTime;

public record AvailableTimeResult(
        long id,
        LocalTime startAt,
        int reservationCount
) {
}
