package roomescape.dto.business;

import java.time.LocalTime;

public record ReservationTimeWithBookCount(
        long id,
        LocalTime startAt,
        long count
) {

}
