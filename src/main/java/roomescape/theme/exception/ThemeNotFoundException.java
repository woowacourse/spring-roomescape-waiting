package roomescape.theme.exception;

import roomescape.global.exception.NotFoundException;

public class ThemeNotFoundException extends NotFoundException {

    public ThemeNotFoundException() {
        super("테마가 존재하지 않습니다.");
    }
}
