package roomescape.payment.infrastructure.dto;

public record ConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
