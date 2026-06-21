package roomescape.payment;

public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}
