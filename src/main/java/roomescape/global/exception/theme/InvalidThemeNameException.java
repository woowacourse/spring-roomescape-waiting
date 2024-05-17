package roomescape.global.exception.theme;

import roomescape.global.exception.IllegalRequestException;

public class InvalidThemeNameException extends IllegalRequestException {

    public InvalidThemeNameException(String message) {
        super(message);
    }

    public InvalidThemeNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
