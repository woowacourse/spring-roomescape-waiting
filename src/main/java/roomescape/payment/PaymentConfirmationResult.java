package roomescape.payment;

public record PaymentConfirmationResult(
        PaymentResult paymentResult,
        boolean unknown
) {

    public static PaymentConfirmationResult success(PaymentResult paymentResult) {
        return new PaymentConfirmationResult(paymentResult, false);
    }

    public static PaymentConfirmationResult unknownResult() {
        return new PaymentConfirmationResult(null, true);
    }
}
