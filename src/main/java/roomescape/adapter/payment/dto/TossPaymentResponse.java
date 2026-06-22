package roomescape.adapter.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Toss 결제 승인 성공 응답. 쓰지 않는 필드는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        Long totalAmount,
        String method,
        String approvedAt
) {
}
