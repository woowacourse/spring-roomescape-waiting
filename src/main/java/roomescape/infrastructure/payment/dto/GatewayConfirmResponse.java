package roomescape.infrastructure.payment.dto;

public record GatewayConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        long totalAmount
) {
}
