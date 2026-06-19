package roomescape.payment;

public record PaymentFailure(
        String code,
        String message,
        String orderId
) {
}
