package roomescape.payment.toss.dto;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        long amount
) {
}
