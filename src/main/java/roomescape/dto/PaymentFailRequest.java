package roomescape.dto;

public record PaymentFailRequest(
        String code,
        String message,
        String orderId
) {
}
