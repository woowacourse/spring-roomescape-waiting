package roomescape.exception.clienterror;

import roomescape.exception.ErrorType;
import roomescape.exception.InvalidClientFieldWithValueException;

public class InvalidDataTypeException extends InvalidClientFieldWithValueException {
    public InvalidDataTypeException(final String fieldName, final String value) {
        super(ErrorType.INVALID_DATA_TYPE, fieldName, value);
    }
}
