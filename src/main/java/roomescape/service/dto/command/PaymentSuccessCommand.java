package roomescape.service.dto.command;

import jakarta.validation.constraints.NotNull;

public record PaymentSuccessCommand(
        @NotNull(message = "주문 ID는 비워둘 수 없습니다.")
        String orderId,

        @NotNull(message = "주문 금액은 비워둘 수 없습니다.")
        Long price,

        @NotNull(message = "결제 키는 비워둘 수 없습니다.")
        String paymentKey
) {
}
