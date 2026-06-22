package roomescape.payment.domain.exception;

public class OutboundRateLimitException extends PaymentRetryableException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}
