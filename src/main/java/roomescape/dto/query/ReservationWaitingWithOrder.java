package roomescape.dto.query;

import roomescape.domain.ReservationWaiting;

public record ReservationWaitingWithOrder(
        ReservationWaiting reservationWaiting,
        int order
) {
}
