package roomescape.domain.payment;

import java.time.OffsetDateTime;

public record PaymentResult(
        String paymentKey,
        String orderId,
        long amount,
        PaymentStatus status,
        OffsetDateTime approvedAt
) {

    public boolean isApproved() {
        return status == PaymentStatus.APPROVED;
    }
}
