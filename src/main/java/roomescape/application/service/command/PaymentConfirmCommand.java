package roomescape.application.service.command;

public record PaymentConfirmCommand(
        String paymentKey,
        String orderId,
        long amount
) {
}
