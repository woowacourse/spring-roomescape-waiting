package roomescape.payment.client;

public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}