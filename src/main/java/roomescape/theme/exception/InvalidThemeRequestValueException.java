package roomescape.theme.exception;

import roomescape.global.exception.InvalidRequestValueException;

public class InvalidThemeRequestValueException extends InvalidRequestValueException {

    public InvalidThemeRequestValueException() {
        super("테마 요청 값이 유효하지 않습니다.");
    }
}
