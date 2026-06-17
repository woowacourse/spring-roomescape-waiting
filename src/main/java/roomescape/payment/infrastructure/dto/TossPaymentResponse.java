package roomescape.payment.infrastructure.dto;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Long totalAmount,
        Long balanceAmount,
        String method,
        String approvedAt,
        String requestedAt
) {
}
