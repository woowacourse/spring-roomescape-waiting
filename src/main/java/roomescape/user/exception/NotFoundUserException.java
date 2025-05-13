package roomescape.user.exception;

import roomescape.global.exception.NotFoundException;

public class NotFoundUserException extends NotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 유저를 찾을 수 없습니다";

    public NotFoundUserException(String message) {
        super(message);
    }

    public NotFoundUserException() {
        this(DEFAULT_MESSAGE);
    }
}
