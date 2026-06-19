package roomescape.reservation.application.dto;

public record PaymentConfirmCommand(
        String paymentKey,
        String orderId,
        Long amount
) {
}
