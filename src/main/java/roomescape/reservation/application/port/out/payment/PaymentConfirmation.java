package roomescape.reservation.application.port.out.payment;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount
) {
}
