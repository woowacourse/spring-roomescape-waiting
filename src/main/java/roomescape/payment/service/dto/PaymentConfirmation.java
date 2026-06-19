package roomescape.payment.service.dto;

public record PaymentConfirmation(
        String paymentKey,
        String orderId,
        Long amount
) {

}
