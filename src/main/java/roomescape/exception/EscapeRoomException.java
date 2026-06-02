package roomescape.exception;

import lombok.Getter;

@Getter
public class EscapeRoomException extends RuntimeException {
    private final ErrorCode errorCode;

    public EscapeRoomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public EscapeRoomException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
    }
}
