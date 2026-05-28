package roomescape.exception.server;

import roomescape.exception.RoomEscapeException;

public abstract class RoomEscapeServerException extends RoomEscapeException {

    protected RoomEscapeServerException(String message) {
        super(message);
    }
}
