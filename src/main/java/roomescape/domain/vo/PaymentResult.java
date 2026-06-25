package roomescape.domain.vo;

public record PaymentResult(
        String paymentKey,
        String orderId,
        Long amount
) {
}
