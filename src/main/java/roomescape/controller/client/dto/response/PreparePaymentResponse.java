package roomescape.controller.client.dto.response;

public record PreparePaymentResponse(
        String orderId,
        Long amount,
        String orderName,
        String clientKey
) {
}
