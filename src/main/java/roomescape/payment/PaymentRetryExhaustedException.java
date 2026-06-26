package roomescape.payment;

public class PaymentRetryExhaustedException extends RuntimeException {

    public PaymentRetryExhaustedException(String message) {
        super(message);
    }
}
