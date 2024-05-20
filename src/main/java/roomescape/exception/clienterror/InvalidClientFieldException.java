package roomescape.exception.clienterror;

import org.springframework.http.HttpStatus;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorType;

public class InvalidClientFieldException extends CustomException {
    private static final String MESSAGE_FORMAT = "%s 필드명 : [%s]";

    private final ErrorType errorType;
    private final String fieldName;

    public InvalidClientFieldException(final ErrorType errorType, final String fieldName) {
        super(HttpStatus.BAD_REQUEST, String.format(MESSAGE_FORMAT, errorType.getMessage(), fieldName));
        this.errorType = errorType;
        this.fieldName = fieldName;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
