package roomescape.controller.dto.request;

public record ControllerPaymentFailRequest(
        String code,
        String message,
        String orderId
) {
}
