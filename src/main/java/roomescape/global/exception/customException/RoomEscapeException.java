package roomescape.global.exception.customException;

import lombok.Getter;
import roomescape.global.exception.ErrorCode;

@Getter
public class RoomEscapeException extends RuntimeException {

    private final ErrorCode errorCode;

    public RoomEscapeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public RoomEscapeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
