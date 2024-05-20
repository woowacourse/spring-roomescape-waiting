package roomescape.exception.clienterror;

import roomescape.exception.ErrorType;

public class InvalidIdException extends InvalidClientFieldWithValueException {
    public InvalidIdException(final String fieldName, final Long id) {
        super(ErrorType.INVALID_ID, fieldName, id.toString());
    }
}
