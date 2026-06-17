package roomescape.service.dto;

import roomescape.domain.PaymentOrderStatus;

public record ReservationPayment(
        String orderId,
        PaymentOrderStatus status,
        String paymentKey,
        Long amount
) {
}
