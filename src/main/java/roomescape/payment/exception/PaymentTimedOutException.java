package roomescape.payment.exception;

public class PaymentTimedOutException extends RuntimeException {

    public PaymentTimedOutException(Throwable cause) {
        super(cause);
    }
}
