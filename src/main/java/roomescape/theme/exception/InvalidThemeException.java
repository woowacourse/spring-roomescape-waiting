package roomescape.theme.exception;

import roomescape.common.exception.ValidationException;

public class InvalidThemeException extends ValidationException {

    public InvalidThemeException(final String message) {
        super(message);
    }
}
