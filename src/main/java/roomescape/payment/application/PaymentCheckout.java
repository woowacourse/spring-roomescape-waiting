package roomescape.payment.application;

import roomescape.reservation.Reservation;

public record PaymentCheckout(
        Reservation reservation,
        String orderId,
        String orderName,
        Long amount
) {
}
