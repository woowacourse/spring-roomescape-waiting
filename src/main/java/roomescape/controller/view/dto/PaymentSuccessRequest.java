package roomescape.controller.view.dto;

public record PaymentSuccessRequest(
        String paymentKey,
        String orderId,
        Long amount,
        String name
) {
}
