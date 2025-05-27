package roomescape.service.dto.result;

import java.time.LocalTime;

public record AvailableReservationTimeResult(
        Long timeId,
        LocalTime startAt,
        boolean booked
) {
}
