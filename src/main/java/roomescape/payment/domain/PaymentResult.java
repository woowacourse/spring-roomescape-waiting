package roomescape.payment.domain;

public record PaymentResult(
        String paymentKey,
        String orderId,
        long amount
) {
}
