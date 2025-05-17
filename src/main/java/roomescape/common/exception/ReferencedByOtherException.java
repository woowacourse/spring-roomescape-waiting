package roomescape.common.exception;

public class ReferencedByOtherException extends CustomException {

    public ReferencedByOtherException(String message) {
        super(message, ErrorCode.CONFLICT);
    }

    public ReferencedByOtherException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
