package roomescape.exception.server;

public class OutboundRateLimitException extends RoomEscapeServerException {

    public OutboundRateLimitException(String message) {
        super(message);
    }
}
