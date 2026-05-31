package roomescape.support.exception;

import lombok.Getter;

@Getter
public class RoomescapeException extends RuntimeException {

    private final ErrorCode errorCode;

    public RoomescapeException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }
}
