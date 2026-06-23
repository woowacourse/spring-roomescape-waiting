package roomescape.payment.ui.dto;

public record PaymentFailRequest(
        String code,
        String message,
        String orderId
) {
}
