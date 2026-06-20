package roomescape.dto.request;

public record PaymentConfirmRequest(String paymentKey, String orderId, Long amount) {
}
