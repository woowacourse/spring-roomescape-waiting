package roomescape.payment.domain.exception;

public class PaymentConfirmationPendingException extends RuntimeException {
    public static final String CODE = "PAYMENT_CONFIRM_PENDING";

    public PaymentConfirmationPendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
