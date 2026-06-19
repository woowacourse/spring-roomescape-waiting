package roomescape.exception.PaymentException;

public class PaymentRateLimitException extends RuntimeException {

    public PaymentRateLimitException(String message) {
        super(message);
    }
}
