package roomescape.domain;

public record PaymentOrder(
        String orderId,
        Long reservationId,
        Long amount,
        String idempotencyKey,
        String paymentKey,
        PaymentOrderStatus status
) {
}
