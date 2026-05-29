package roomescape.exception;

public class BusinessException extends RoomEscapeException {

    public BusinessException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
