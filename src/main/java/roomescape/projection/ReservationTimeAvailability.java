package roomescape.projection;

import roomescape.domain.ReservationTime;

public record ReservationTimeAvailability(
        ReservationTime time,
        boolean available
) {
}
