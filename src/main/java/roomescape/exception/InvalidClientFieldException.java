package roomescape.exception;

public class InvalidClientFieldException extends BadRequestException {
    private final ErrorType errorType;
    private final String fieldName;

    public InvalidClientFieldException(final ErrorType errorType, final String fieldName) {
        super();
        this.errorType = errorType;
        this.fieldName = fieldName;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getFieldName() {
        return fieldName;
    }
}
