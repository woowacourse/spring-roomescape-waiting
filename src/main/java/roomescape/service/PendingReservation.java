package roomescape.service;

import payment.OrderTicket;
import roomescape.domain.Reservation;

public record PendingReservation(
        Reservation reservation,
        String orderId,
        Long amount,
        String orderName
) {

    public static PendingReservation of(Reservation reservation, OrderTicket ticket) {
        return new PendingReservation(
                reservation,
                ticket.orderId(),
                ticket.amount(),
                reservation.slot().theme().name() + " 예약"
        );
    }
}
