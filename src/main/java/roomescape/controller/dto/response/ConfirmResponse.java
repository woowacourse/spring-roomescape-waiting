package roomescape.controller.dto.response;

public record ConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        Long totalAmount
) {
}
