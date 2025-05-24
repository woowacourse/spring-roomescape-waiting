package roomescape.waiting.exception;

import roomescape.global.exception.InvalidInputException;

public class InvalidWaitingException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "잘못된 예약 대기입니다.";

    public InvalidWaitingException(String message) {
        super(message);
    }

    public InvalidWaitingException() {
        this(DEFAULT_MESSAGE);
    }
}
