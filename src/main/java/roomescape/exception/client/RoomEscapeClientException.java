package roomescape.exception.client;

import roomescape.exception.RoomEscapeException;

public abstract class RoomEscapeClientException extends RoomEscapeException {

    protected RoomEscapeClientException(String message) {
        super(message);
    }
}
