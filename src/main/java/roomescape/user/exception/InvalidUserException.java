package roomescape.user.exception;

import roomescape.global.exception.InvalidInputException;

public class InvalidUserException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 유저입니다.";

    public InvalidUserException(String message) {
        super(message);
    }

    public InvalidUserException() {
        this(DEFAULT_MESSAGE);
    }
}
