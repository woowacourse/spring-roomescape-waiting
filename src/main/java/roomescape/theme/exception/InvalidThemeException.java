package roomescape.theme.exception;

import roomescape.exception.ValidationException;

public class InvalidThemeException extends ValidationException {

    public InvalidThemeException(final String message) {
        super(message);
    }
}
