package roomescape.exception.custom.reason.theme;

import roomescape.exception.custom.status.NotFoundException;

public class ThemeNotFoundException extends NotFoundException {

    public ThemeNotFoundException() {
        super("존재하지 않는 테마입니다.");
    }
}
