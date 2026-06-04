package roomescape.dto;

import java.time.LocalTime;

public record ReservationTimeStatusResult(
        Long id,
        LocalTime startAt,
        boolean reserved
) {
}
