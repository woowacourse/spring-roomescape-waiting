package roomescape.theme.exception;

import roomescape.global.exception.InvalidInputException;

public class InvalidThemeException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 테마입니다.";

    public InvalidThemeException(String message) {
        super(message);
    }

    public InvalidThemeException() {
        this(DEFAULT_MESSAGE);
    }
}
