package roomescape.payment.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.payment.domain.PaymentStatus;

@Builder
public record PaymentResult(
        String paymentKey,
        String orderId,
        PaymentStatus status,
        Long approvedAmount,
        LocalDateTime createdAt
) {
}
