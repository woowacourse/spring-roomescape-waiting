package roomescape.dto.payment;

public record PaymentConfirmRequest(String paymentKey, String orderId, long amount) {
}
