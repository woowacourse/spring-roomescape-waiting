package roomescape.infrastructure.payment;

public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}