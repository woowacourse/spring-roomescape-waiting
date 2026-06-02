package roomescape.theme.exception;

import roomescape.global.exception.InvalidRequestFormatException;

public class InvalidThemeRequestFormatException extends InvalidRequestFormatException {

    public InvalidThemeRequestFormatException() {
        super("테마 요청 형식이 유효하지 않습니다.");
    }
}
