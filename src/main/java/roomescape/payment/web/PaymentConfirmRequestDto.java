package roomescape.payment.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequestDto(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @Positive long amount
) {
}
