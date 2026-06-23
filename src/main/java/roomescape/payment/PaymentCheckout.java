package roomescape.payment;

import roomescape.reservation.Reservation;

public record PaymentCheckout(
        Reservation reservation,
        String orderId,
        String orderName,
        Long amount
) {
}
