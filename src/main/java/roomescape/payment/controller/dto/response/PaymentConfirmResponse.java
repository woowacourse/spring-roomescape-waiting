package roomescape.payment.controller.dto.response;

public record PaymentConfirmResponse(
        String orderId,
        int amount,
        String paymentKey,
        String reservationStatus,
        String paymentStatus,
        String message
) {
}
