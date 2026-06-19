package roomescape.payment.exception;

public class PaymentConnectionFailedException extends RuntimeException {

    public PaymentConnectionFailedException(Throwable cause) {
        super(cause);
    }
}
