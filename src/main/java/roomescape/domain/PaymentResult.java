package roomescape.domain;

public record PaymentResult(String paymentKey, String orderId, PaymentStatus status) {
}
