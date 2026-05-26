package roomescape.dto;

import java.time.LocalTime;

public record ReservationTimesWithStatus(
        Long id,
        LocalTime startAt,
        boolean reserved
) {
}
