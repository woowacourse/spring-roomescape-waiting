package roomescape.payment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentFailRequest(
        @NotBlank(message = "결제 실패 코드를 입력해야 합니다.")
        String code,
        @NotBlank(message = "결제 실패 사유를 입력해야 합니다.")
        String message,
        String orderId
) {
}
