package roomescape.exception.local;

import roomescape.exception.global.NotFoundException;

public class NotFoundWaitingException extends NotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 대기 데이터를 찾을 수 없습니다.";

    public NotFoundWaitingException(String message) {
        super(message);
    }

    public NotFoundWaitingException() {
        this(DEFAULT_MESSAGE);
    }
}

