package roomescape.feature.payment.dto;

public record PaymentApproveRequest(
        String orderId,
        String paymentKey,
        Long amount
) {
}
