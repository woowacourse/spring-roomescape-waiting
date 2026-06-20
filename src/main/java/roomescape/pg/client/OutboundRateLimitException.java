package roomescape.pg.client;

public class OutboundRateLimitException extends RuntimeException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}
