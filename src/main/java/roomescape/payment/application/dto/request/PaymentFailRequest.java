package roomescape.payment.application.dto.request;

public record PaymentFailRequest(
        String code,
        String message,
        String orderId
) {
}
