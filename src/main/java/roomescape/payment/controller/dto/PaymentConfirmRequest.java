package roomescape.payment.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotBlank(message = "결제 키는 비어 있을 수 없습니다.")
        String paymentKey,

        @NotBlank(message = "주문번호는 비어 있을 수 없습니다.")
        String orderId,

        @NotNull(message = "결제 금액은 비어 있을 수 없습니다.")
        @Positive(message = "결제 금액은 1원 이상이어야 합니다.")
        Long amount
) {
}
