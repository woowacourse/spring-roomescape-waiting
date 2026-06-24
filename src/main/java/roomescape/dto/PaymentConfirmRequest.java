package roomescape.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotBlank String paymentKey,
        @Positive long amount
) {
}
