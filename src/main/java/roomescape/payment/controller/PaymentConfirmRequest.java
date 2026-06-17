package roomescape.payment.controller;

public record PaymentConfirmRequest(String paymentKey, String orderId, Long amount) {
}
