package roomescape.payment.repository.entity;

public record PaymentOrderEntity(
        Long id,
        String orderId,
        int amount,
        String paymentKey,
        String status,
        Long reservationId
) {
}
