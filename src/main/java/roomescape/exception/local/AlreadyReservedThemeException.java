package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class AlreadyReservedThemeException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "예약에서 사용 중인 테마입니다.";

    public AlreadyReservedThemeException(String message) {
        super(message);
    }

    public AlreadyReservedThemeException() {
        this(DEFAULT_MESSAGE);
    }
}
