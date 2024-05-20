package roomescape.exception.clienterror;

import org.springframework.http.HttpStatus;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorType;

public class InvalidClientFieldWithValueException extends CustomException {
    private static final String MESSAGE_FORMAT_WITH_VALUE = "%s 필드명 : [%s], 값 : [%s]";

    private final ErrorType errorType;
    private final String fieldName;
    private final String value;

    public InvalidClientFieldWithValueException(final ErrorType errorType, final String fieldName, final String value) {
        super(HttpStatus.BAD_REQUEST,
                String.format(MESSAGE_FORMAT_WITH_VALUE, errorType.getMessage(), fieldName, value));
        this.errorType = errorType;
        this.fieldName = fieldName;
        this.value = value;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
