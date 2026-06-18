package roomescape.pg;

public record PaymentConfirmation(
    String paymentKey,
    String orderId,
    Long amount
) {

}
