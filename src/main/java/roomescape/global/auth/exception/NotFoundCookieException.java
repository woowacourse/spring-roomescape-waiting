package roomescape.global.auth.exception;

import roomescape.global.exception.AuthorizationException;

public class NotFoundCookieException extends AuthorizationException {

    private static final String DEFAULT_MESSAGE = "쿠키가 존재하지 않습니다.";

    public NotFoundCookieException(String message) {
        super(message);
    }

    public NotFoundCookieException() {
        this(DEFAULT_MESSAGE);
    }
}
