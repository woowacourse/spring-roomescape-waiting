package roomescape.common.exception;

public abstract class BusinessException extends RuntimeException {

    private final ErrorType errorType;

    protected BusinessException(ErrorType errorType) {
        super(errorType.getErrorMessage());
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
