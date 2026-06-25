package roomescape.repository.dto;

import java.time.LocalTime;

public record ReservationTimesWithStatus(
        Long id,
        LocalTime startAt,
        boolean reserved
) {
}
