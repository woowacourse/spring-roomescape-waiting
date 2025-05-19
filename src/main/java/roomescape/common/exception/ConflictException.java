package roomescape.common.exception;

public class ConflictException extends CustomException {

    public ConflictException(String message) {
        super(message, ErrorCode.CONFLICT);
    }

    public ConflictException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
