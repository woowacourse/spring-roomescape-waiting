package roomescape.waiting.exception;

import roomescape.global.exception.NotFoundException;

public class NotFoundWaitingException extends NotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 예약 대기를 찾을 수 없습니다.";

    public NotFoundWaitingException(String message) {
        super(message);
    }

    public NotFoundWaitingException() {
        this(DEFAULT_MESSAGE);
    }
}
