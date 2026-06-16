package roomescape.payment.controller.dto;

public record PaymentFailRequest(
        String code,
        String message,
        String orderId
) {
}
