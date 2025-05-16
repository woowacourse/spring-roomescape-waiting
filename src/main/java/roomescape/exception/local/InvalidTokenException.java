package roomescape.exception.local;

import roomescape.exception.global.AuthorizationException;

public class InvalidTokenException extends AuthorizationException {

    private static final String DEFAULT_MESSAGE = "잘못된 토큰입니다.";

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
        this(DEFAULT_MESSAGE);
    }
}
