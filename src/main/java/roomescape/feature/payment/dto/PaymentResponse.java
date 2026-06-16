package roomescape.feature.payment.dto;

import roomescape.feature.payment.PaymentStatus;

public record PaymentResponse(
        PaymentStatus status,
        String paymentKey
) {
}
