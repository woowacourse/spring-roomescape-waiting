package roomescape.infrastructure;

import java.time.OffsetDateTime;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;

public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        long totalAmount,
        String status,
        OffsetDateTime approvedAt
) {

    public PaymentResult toPaymentResult() {
        return new PaymentResult(paymentKey, orderId, totalAmount, PaymentStatus.getStatus(status), approvedAt);
    }
}
