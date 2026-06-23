package roomescape.payment;

import roomescape.domain.PaymentStatus;

public record PaymentResult(
        String paymentKey,
        String orderId,
        PaymentStatus status,
        Long approvedAmount
) {
}
