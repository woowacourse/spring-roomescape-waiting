package roomescape.payment.presentation.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.payment.application.dto.PaymentResult;

@Builder
public record PaymentCancelResponse(
        String orderId,
        String paymentKey,
        String status,
        Long canceledAmount,
        Long remainingAmount,
        LocalDateTime canceledAt
) {
    public static PaymentCancelResponse from(String orderId, PaymentResult result) {
        return PaymentCancelResponse.builder()
                .orderId(orderId)
                .paymentKey(result.paymentKey())
                .status(result.status().name())
                .canceledAmount(result.approvedAmount())
                .remainingAmount(0L)
                .canceledAt(result.createdAt())
                .build();
    }
}
