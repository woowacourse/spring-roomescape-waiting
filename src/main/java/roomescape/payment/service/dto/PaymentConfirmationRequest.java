package roomescape.payment.service.dto;

public record PaymentConfirmationRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
