package roomescape.dto.query;

import roomescape.domain.ReservationWaiting;

public record ReservationWaitingWIthOrder(
        ReservationWaiting reservationWaiting,
        int order
) {
}
