package roomescape.dto.response;

public record PaymentCheckoutResponse(
        String clientKey,
        String orderId,
        Long amount,
        String orderName
) {
}
