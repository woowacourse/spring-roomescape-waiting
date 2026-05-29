package roomescape.exception;

import lombok.Getter;

@Getter
public class RoomEscapeException extends RuntimeException {

    private final ErrorCode errorCode;

    public RoomEscapeException(final ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
