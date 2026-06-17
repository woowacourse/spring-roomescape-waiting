package roomescape.infra.payment;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Long totalAmount,
        String method,
        String approvedAt
) {
}
