package roomescape.payment.order;

public record PaymentOrder(
        Long id,
        String orderId,
        Long amount,
        Long reservationId,
        String idempotencyKey,
        PaymentOrderStatus status,
        String paymentKey,
        Long approvedAmount
) {
}
