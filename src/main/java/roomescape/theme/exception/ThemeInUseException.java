package roomescape.theme.exception;

import roomescape.global.exception.DeleteFailedException;

public class ThemeInUseException extends DeleteFailedException {

    public ThemeInUseException() {
        super("해당 테마의 예약이 존재합니다.");
    }
}
