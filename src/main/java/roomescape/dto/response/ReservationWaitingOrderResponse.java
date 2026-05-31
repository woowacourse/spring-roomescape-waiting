package roomescape.dto.response;

import roomescape.domain.ReservationWaiting;

public record ReservationWaitingOrderResponse(
        ReservationWaiting waiting,
        int order
) {
}
