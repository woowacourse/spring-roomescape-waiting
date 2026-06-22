package roomescape.payment.exception;

public class PaymentCommunicationException extends RuntimeException {

    public PaymentCommunicationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
