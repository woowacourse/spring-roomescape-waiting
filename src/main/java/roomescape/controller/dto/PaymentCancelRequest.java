package roomescape.controller.dto;

public record PaymentCancelRequest(
        String code,
        String message,
        String orderId
) {
}
