package roomescape.global.exception;

public class BusinessException extends RuntimeException {
    private final ErrorResponse errorResponse;
    private String description;

    public BusinessException(final ErrorType errorType) {
        super(errorType.getErrorMessage());
        this.errorResponse = new ErrorResponse(
                errorType.getHttpStatus(),
                errorType.getErrorMessage()
        );
    }

    public BusinessException(final ErrorType errorType, final String description) {
        this.errorResponse = new ErrorResponse(
                errorType.getHttpStatus(),
                errorType.getErrorMessage()
        );
        this.description = description;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
