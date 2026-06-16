package roomescape.payment.client.dto;

public record ConfirmRequest(String paymentKey, String orderId, long amount) {
}