package roomescape.controller.dto.payment;

public record PaymentFailResponse(
        String code,
        String message,
        String orderId
) {
}
