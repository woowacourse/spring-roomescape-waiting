package roomescape.infrastructure.payment.client.dto;

/**
 * Toss 결제 승인 API 요청 바디.
 * POST https://api.tosspayments.com/v1/payments/confirm
 */
public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        long amount
) {
}
