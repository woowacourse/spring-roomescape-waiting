package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class AlreadyWaitingTimeException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "이미 예약대기 중인 예약시간입니다.";

    public AlreadyWaitingTimeException(String message) {
        super(message);
    }

    public AlreadyWaitingTimeException() {
        this(DEFAULT_MESSAGE);
    }
}
