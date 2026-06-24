package roomescape.domain;

public record PaymentConfirmation(String paymentKey, String orderId, Long amount) {
}
