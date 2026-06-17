package roomescape.payment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        Long totalAmount
) {
    public PaymentResult toResult() {
        return new PaymentResult(paymentKey, orderId, parseStatus(status), totalAmount);
    }

    private PaymentStatus parseStatus(String status) {
        try {
            return PaymentStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return PaymentStatus.UNKNOWN;
        }
    }
}
