package roomescape.repository.exception;

import roomescape.exception.NotFoundException;

public class ThemeNotFoundException extends NotFoundException {

    public ThemeNotFoundException() {
        super("테마가 존재하지 않습니다.");
    }
}
