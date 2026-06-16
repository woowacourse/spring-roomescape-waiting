package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import roomescape.reservation.application.dto.PaymentConfirmCommand;

public record PaymentConfirmRequest(
        @NotBlank(message = "결제 키는 비어있을 수 없습니다.")
        String paymentKey,
        @NotBlank(message = "주문 ID는 비어있을 수 없습니다.")
        String orderId,
        @NotNull(message = "결제 금액은 비어있을 수 없습니다.")
        @Positive(message = "결제 금액은 양수여야 합니다.")
        Long amount
) {

    public PaymentConfirmCommand toCommand() {
        return new PaymentConfirmCommand(paymentKey, orderId, amount);
    }
}
