package roomescape.payment.toss.dto;

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
    public PaymentResult toPaymentResult() {
        return new PaymentResult(paymentKey, orderId, PaymentStatus.from(status), totalAmount);
    }
}
