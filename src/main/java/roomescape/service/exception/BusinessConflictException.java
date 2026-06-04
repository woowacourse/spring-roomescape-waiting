package roomescape.service.exception;

public class BusinessConflictException extends BusinessException {

    public BusinessConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}
