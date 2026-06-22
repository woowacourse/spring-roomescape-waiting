package roomescape.payment.web.dto;

public record PaymentFailRequest(
        String code,
        String message,
        String orderId
) {
}
