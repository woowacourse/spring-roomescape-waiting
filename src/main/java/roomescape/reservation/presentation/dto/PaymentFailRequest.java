package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import roomescape.reservation.application.dto.PaymentFailCommand;

public record PaymentFailRequest(
        @NotBlank(message = "실패 코드는 비어있을 수 없습니다.")
        String code,
        @NotBlank(message = "실패 메시지는 비어있을 수 없습니다.")
        String message,
        String orderId
) {

    public PaymentFailCommand toCommand(LocalDateTime now) {
        return new PaymentFailCommand(orderId, now);
    }
}
