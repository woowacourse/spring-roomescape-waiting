package roomescape.payment.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.payment.infra.client.PaymentStatus;

@Builder
public record PaymentResult(
        String paymentKey,
        String orderId,
        PaymentStatus status,
        Long approvedAmount,
        LocalDateTime createdAt
) {
}
