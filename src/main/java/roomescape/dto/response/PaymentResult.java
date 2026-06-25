package roomescape.dto.response;

import roomescape.domain.PaymentStatus;

public record PaymentResult(
        String paymentKey,
        String orderId,
        PaymentStatus status,
        Long approvedAmount
) {
}
