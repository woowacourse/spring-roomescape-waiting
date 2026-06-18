package roomescape.payment;

import roomescape.domain.exception.DomainErrorCode;

public record PaymentConfirmationResult(
        PaymentResult paymentResult,
        boolean unknown,
        DomainErrorCode failureCode
) {

    public static PaymentConfirmationResult success(PaymentResult paymentResult) {
        return new PaymentConfirmationResult(paymentResult, false, null);
    }

    public static PaymentConfirmationResult unknownResult() {
        return new PaymentConfirmationResult(null, true, null);
    }

    public static PaymentConfirmationResult failure(DomainErrorCode failureCode) {
        return new PaymentConfirmationResult(null, false, failureCode);
    }

    public boolean failed() {
        return failureCode != null;
    }
}
