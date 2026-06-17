package roomescape.service;

import roomescape.domain.Reservation;

public record PendingReservation(
        Reservation reservation,
        String orderId,
        Long amount,
        String orderName
) {
}
