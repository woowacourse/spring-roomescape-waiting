package roomescape.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Long totalAmount,
        String method,
        String approvedAt
) {
    public Long approvedAmount() {
        return totalAmount;
    }
}
