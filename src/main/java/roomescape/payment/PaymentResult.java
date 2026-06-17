package roomescape.payment;

public record PaymentResult(
        String paymentKey,
        String orderId,
        String status,
        long approvedAmount
) {
}
