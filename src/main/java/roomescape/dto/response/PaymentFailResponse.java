package roomescape.dto.response;

public record PaymentFailResponse(
        String code,
        String message,
        String orderId
) {
}
