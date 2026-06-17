package roomescape.payment.adapter.out.toss.dto;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        int amount
) {
}
