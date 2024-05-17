package roomescape.global.exception.theme;

import roomescape.global.exception.IllegalRequestException;

public class InvalidDescriptionException extends IllegalRequestException {

    public InvalidDescriptionException(String message) {
        super(message);
    }

    public InvalidDescriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
