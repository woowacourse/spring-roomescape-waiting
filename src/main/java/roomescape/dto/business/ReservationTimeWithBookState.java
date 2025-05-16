package roomescape.dto.business;

import java.time.LocalTime;

public record ReservationTimeWithBookState(
        long id,
        LocalTime startAt,
        boolean alreadyBooked
) {

}
