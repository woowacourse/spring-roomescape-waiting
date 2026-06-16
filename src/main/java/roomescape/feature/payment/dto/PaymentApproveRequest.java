package roomescape.feature.payment.dto;

public record PaymentApproveRequest(
        Long orderId,
        String paymentKey,
        Long amount
) {
}
