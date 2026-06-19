package roomescape.payment.adapter.out.toss.dto;

public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        int totalAmount
) {
}
