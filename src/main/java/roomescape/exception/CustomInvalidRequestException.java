package roomescape.exception;

public class CustomInvalidRequestException extends CustomException {

    public CustomInvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
