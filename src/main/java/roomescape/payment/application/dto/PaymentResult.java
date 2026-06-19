package roomescape.payment.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.payment.domain.OrderStatus;

@Builder
public record PaymentResult(
        String paymentKey,
        String orderId,
        OrderStatus status,
        Long approvedAmount,
        LocalDateTime createdAt
) {
}
