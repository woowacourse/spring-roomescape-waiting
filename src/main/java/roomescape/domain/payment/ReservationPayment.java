package roomescape.domain.payment;

import java.util.UUID;

public record ReservationPayment(
    Long id,
    Long reservationId,
    String orderId,
    String paymentKey,
    Long amount,
    String idempotencyKey,
    PaymentStatus status
) {

    public static ReservationPayment pending(Long reservationId, Long amount) {
        return new ReservationPayment(
            null,
            reservationId,
            UUID.randomUUID().toString(),
            null,
            amount,
            UUID.randomUUID().toString(),
            PaymentStatus.PENDING
        );
    }
}
