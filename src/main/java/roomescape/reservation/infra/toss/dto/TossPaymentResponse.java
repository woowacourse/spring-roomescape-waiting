package roomescape.reservation.infra.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;

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

    public PaymentResult toResult() {
        return new PaymentResult(paymentKey, orderId, PaymentStatus.from(status), totalAmount);
    }
}
