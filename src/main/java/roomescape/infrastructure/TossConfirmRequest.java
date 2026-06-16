package roomescape.infrastructure;

public record TossConfirmRequest(String paymentKey, String orderId, long amount) {
}
