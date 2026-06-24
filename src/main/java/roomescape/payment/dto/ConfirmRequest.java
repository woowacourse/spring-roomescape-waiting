package roomescape.payment.dto;

public record ConfirmRequest(String paymentKey, String orderId, Long amount) {
}
