package roomescape.payment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotBlank(message = "결제 키를 입력해야 합니다.")
        String paymentKey,
        @NotBlank(message = "주문 번호를 입력해야 합니다.")
        String orderId,
        @Positive(message = "결제 금액은 양수여야 합니다.")
        int amount
) {
}
