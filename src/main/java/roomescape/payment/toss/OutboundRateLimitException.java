package roomescape.payment.toss;

public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}
