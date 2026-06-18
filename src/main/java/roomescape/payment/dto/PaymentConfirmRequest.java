package roomescape.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotBlank(message = "paymentKey는 필수입니다.")
        String paymentKey,

        @NotBlank(message = "orderId는 필수입니다.")
        String orderId,

        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        long amount
) {
}
