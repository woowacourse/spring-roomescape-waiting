package roomescape.payment.application.dto;

import lombok.Builder;

@Builder
public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount
) {
}
