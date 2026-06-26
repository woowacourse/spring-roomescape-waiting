package roomescape.payment.toss;

public class TossRateLimitException extends TossPaymentException {

    public TossRateLimitException(String message) {
        super(message);
    }
}
