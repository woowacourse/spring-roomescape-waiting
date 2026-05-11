package roomescape.common.exception;

public class InfrastructureException extends RuntimeException {

    private final ErrorCode errorCode;

    public InfrastructureException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
