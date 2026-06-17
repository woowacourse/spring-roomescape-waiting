package roomescape.payment;

public record PaymentOrder(
        Long id,
        String orderId,
        Long amount,
        Long reservationId
) {
}
