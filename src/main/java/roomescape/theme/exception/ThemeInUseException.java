package roomescape.theme.exception;

import roomescape.global.exception.DeleteFailedException;

public class ThemeInUseException extends DeleteFailedException {

    public ThemeInUseException() {
        super("해당 테마의 예약 또는 예약 대기가 존재합니다.");
    }
}
