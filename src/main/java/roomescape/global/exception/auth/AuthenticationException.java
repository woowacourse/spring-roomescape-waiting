package roomescape.global.exception.auth;

import roomescape.global.exception.IllegalRequestException;

public class AuthenticationException extends IllegalRequestException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
