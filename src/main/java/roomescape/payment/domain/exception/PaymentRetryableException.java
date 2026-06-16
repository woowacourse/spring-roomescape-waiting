package roomescape.payment.domain.exception;

public class PaymentRetryableException extends RuntimeException {

    public PaymentRetryableException(String message) {
        super(message);
    }
}
