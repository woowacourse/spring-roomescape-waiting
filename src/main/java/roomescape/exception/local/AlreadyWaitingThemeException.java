package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class AlreadyWaitingThemeException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "이미 예약대기 중인 테마입니다.";

    public AlreadyWaitingThemeException(String message) {
        super(message);
    }

    public AlreadyWaitingThemeException() {
        this(DEFAULT_MESSAGE);
    }
}
