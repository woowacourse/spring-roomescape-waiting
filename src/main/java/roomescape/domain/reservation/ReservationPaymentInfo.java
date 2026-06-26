package roomescape.domain.reservation;

import roomescape.domain.payment.OrderStatus;

public record ReservationPaymentInfo(
        OrderStatus status,
        String orderId,
        String paymentKey,
        Long amount
) {
}
