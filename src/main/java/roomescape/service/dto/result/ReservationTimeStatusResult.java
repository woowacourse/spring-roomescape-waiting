package roomescape.service.dto.result;

import java.time.LocalTime;

public record ReservationTimeStatusResult(
        Long id,
        LocalTime startAt,
        boolean reserved
) {
}
