package roomescape.payment.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import roomescape.payment.domain.PaymentResult;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        long totalAmount
) {

    public PaymentResult toResult() {
        return new PaymentResult(paymentKey, orderId, status, totalAmount);
    }
}
