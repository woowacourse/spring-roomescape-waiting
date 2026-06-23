package roomescape.payment.dto;

public record PaymentFailRequest(
        String code,
        String message,
        String orderId
) {
}
