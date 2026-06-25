package roomescape.service.dto.result;

public record PaymentConfirmResult(
        String orderId,
        Long approvedAmount,
        String paymentKey
) {
}
