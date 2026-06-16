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

    /* 부패 방지 계층(ACL): Toss 응답을 코어 모델로 번역 */
    public PaymentResult toPaymentResult() {
        return new PaymentResult(paymentKey, orderId, totalAmount, mapStatus(status), approvedAt);
    }

    private static PaymentStatus mapStatus(String tossStatus) {
        return switch (tossStatus) {
            case "DONE" -> PaymentStatus.APPROVED;
            case "CANCELED", "PARTIAL_CANCELED" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.ABORTED;
        };
    }
}
