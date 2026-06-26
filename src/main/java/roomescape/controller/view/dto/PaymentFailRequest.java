package roomescape.controller.view.dto;

public record PaymentFailRequest(
        String code,
        String message,
        String orderId,
        Long paymentId,
        String name
) {
}
