package roomescape.payment.exception;

public class PaymentApprovalUnknownException extends RuntimeException {

    public PaymentApprovalUnknownException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
