package roomescape.payment.infra.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
