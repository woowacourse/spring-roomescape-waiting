package roomescape.theme.domain.exception;

import roomescape.common.exception.NotFoundException;

public class ThemeNotFoundException extends NotFoundException {

    public ThemeNotFoundException() {
        super("존재하지 않는 테마입니다.");
    }
}
