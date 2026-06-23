package roomescape.service.dto;

public record PaymentConfirmCommand(
        String paymentKey,
        String orderId,
        Long amount
) {
}
