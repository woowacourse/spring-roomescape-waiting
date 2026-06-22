package roomescape.payment.application.dto;

public record PaymentOrderResult(
        String orderId,
        long amount,
        String orderName,
        String clientKey
) {
}
