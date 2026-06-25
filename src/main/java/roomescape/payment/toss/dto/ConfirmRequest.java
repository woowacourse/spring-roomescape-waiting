package roomescape.payment.toss.dto;

public record ConfirmRequest(String paymentKey, String orderId, long amount) {
}
