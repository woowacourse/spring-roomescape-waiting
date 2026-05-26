package roomescape.service.dto.result;

import java.time.LocalTime;

public record ReservationTimeResult(
        Long id,
        LocalTime startAt,
        LocalTime endAt
) {
}
