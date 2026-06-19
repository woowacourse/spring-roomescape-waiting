package roomescape.infrastructure.payment.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Toss 결제 승인 성공 응답. 실제 응답의 모르는 필드는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Long totalAmount,
        Long balanceAmount,
        String method,
        String approvedAt,
        String requestedAt
) {

}
