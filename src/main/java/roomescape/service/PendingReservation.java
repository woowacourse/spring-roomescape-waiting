package roomescape.service;

import payment.order.Order;
import roomescape.domain.Reservation;

public record PendingReservation(
        Reservation reservation,
        String orderId,
        Long amount,
        String orderName
) {

    public static PendingReservation of(Reservation reservation, Order order) {
        return new PendingReservation(
                reservation,
                order.orderId(),
                order.amount(),
                reservation.slot().theme().name() + " 예약"
        );
    }
}
