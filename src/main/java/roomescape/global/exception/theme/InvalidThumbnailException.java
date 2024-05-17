package roomescape.global.exception.theme;

import roomescape.global.exception.IllegalRequestException;

public class InvalidThumbnailException extends IllegalRequestException {

    public InvalidThumbnailException(String message) {
        super(message);
    }

    public InvalidThumbnailException(String message, Throwable cause) {
        super(message, cause);
    }
}
