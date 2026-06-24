package roomescape.domain.vo;

public record PaymentConfirmation(String paymentKey, String orderId, Long amount) {
}
