package roomescape.global.auth.exception;

import roomescape.global.exception.AuthorizationException;

public class InvalidTokenException extends AuthorizationException {

    private static final String DEFAULT_MESSAGE = "잘못된 토큰입니다.";

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
        this(DEFAULT_MESSAGE);
    }
}
