package roomescape.exception.local;

import roomescape.exception.global.NotFoundException;

public class NotFoundThemeException extends NotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 테마를 찾을 수 없습니다.";

    public NotFoundThemeException(String message) {
        super(message);
    }

    public NotFoundThemeException() {
        this(DEFAULT_MESSAGE);
    }
}
