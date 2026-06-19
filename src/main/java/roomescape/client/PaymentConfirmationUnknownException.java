package roomescape.client;

public class PaymentConfirmationUnknownException extends PaymentException {

    public PaymentConfirmationUnknownException(String message, Throwable cause) {
        super("PAYMENT_CONFIRMATION_UNKNOWN", message, cause);
    }
}
