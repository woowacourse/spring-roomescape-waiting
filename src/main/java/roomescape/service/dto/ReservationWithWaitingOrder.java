package roomescape.service.dto;

import roomescape.domain.Reservation;

public record ReservationWithWaitingOrder(
        Reservation reservation,
        int order
) {
}
