package roomescape.controller.dto.response;

public record PaymentResponse(
        String clientKey,
        String orderId,
        String orderName,
        Long amount
) {
}
