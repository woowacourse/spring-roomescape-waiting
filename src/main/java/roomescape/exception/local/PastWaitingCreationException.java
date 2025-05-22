package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class PastWaitingCreationException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "과거의 예약 대기를 추가할 수 없습니다.";

    public PastWaitingCreationException(String message) {
        super(message);
    }

    public PastWaitingCreationException() {
        this(DEFAULT_MESSAGE);
    }
}
