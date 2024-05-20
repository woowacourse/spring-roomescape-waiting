package roomescape.exception.clienterror;

import roomescape.exception.ErrorType;

public class EmptyValueNotAllowedException extends InvalidClientFieldException {
    public EmptyValueNotAllowedException(final String fieldName) {
        super(ErrorType.EMPTY_VALUE_NOT_ALLOWED, fieldName);
    }
}
