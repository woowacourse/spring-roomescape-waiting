package roomescape.domain.payment;

public record PaymentResult(
        String paymentKey,
        String orderId,
        Long totalAmount
) {
}