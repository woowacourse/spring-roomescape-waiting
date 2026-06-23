package roomescape.payment.dto.request;

public record ConfirmRequest(String paymentKey, String orderId, Long amount) {

}