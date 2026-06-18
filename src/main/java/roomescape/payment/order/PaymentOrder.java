package roomescape.payment.order;

public record PaymentOrder(
        Long id,
        String orderId,
        Long amount,
        Long reservationId,
        String idempotencyKey
) {
}
