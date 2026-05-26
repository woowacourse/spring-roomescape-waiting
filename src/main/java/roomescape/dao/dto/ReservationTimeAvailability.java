package roomescape.dao.dto;

import java.time.LocalTime;

public record ReservationTimeAvailability(
        long id,
        LocalTime startAt,
        boolean reserved
) {
}
