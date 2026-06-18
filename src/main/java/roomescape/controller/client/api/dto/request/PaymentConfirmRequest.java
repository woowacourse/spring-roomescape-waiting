package roomescape.controller.client.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import roomescape.application.service.command.PaymentConfirmCommand;

public record PaymentConfirmRequest(
        @NotBlank(message = "결제 키 정보는 필수 입니다.") String paymentKey,
        @NotBlank(message = "주문 ID는 필수입니다.") String orderId,
        @NotNull(message = "결제 가격은 필수 입니다.")
        @Positive(message = "결제 가격은 양수여야 합니다.")
        Long amount
) {
    public PaymentConfirmCommand toCommand() {
        return new PaymentConfirmCommand(paymentKey, orderId, amount);
    }
}
