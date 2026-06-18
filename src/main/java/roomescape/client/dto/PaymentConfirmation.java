package roomescape.client.dto;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount
) {
}
