package roomescape.controller.dto.payment;

public record PaymentOrderResponse(
        String clientKey,
        String orderId,
        String orderName,
        int amount,
        String successUrl,
        String failUrl
) {
}
