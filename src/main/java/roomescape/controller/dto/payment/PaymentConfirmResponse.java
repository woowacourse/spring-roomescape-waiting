package roomescape.controller.dto.payment;

public record PaymentConfirmResponse(
        String orderId,
        String paymentKey,
        Long reservationId,
        int amount,
        String status
) {
}
