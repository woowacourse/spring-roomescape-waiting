package roomescape.global.exception.auth;

import roomescape.global.exception.IllegalRequestException;

public class AuthorizationException extends IllegalRequestException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
