package roomescape.infra.payment;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
