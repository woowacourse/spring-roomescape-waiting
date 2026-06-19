package roomescape.client;

public record PaymentResult(
        String paymentKey,
        String orderId,
        PaymentStatus status,
        Long approvedAmount
) {
}
