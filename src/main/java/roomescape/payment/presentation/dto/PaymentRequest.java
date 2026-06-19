package roomescape.payment.presentation.dto;

public record PaymentRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
