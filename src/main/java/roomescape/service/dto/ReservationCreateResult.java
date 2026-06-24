package roomescape.service.dto;

import roomescape.domain.payment.Order;
import roomescape.domain.reservation.Reservation;

public record ReservationCreateResult(
        Reservation reservation,
        String orderId,
        Long amount
) {

    public static ReservationCreateResult withoutOrder(Reservation reservation) {
        return new ReservationCreateResult(reservation, null, null);
    }

    public static ReservationCreateResult withOrder(Reservation reservation, Order order) {
        return new ReservationCreateResult(reservation, order.getOrderId(), order.getAmount());
    }
}
