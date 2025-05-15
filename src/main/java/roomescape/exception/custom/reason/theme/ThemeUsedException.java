package roomescape.exception.custom.reason.theme;

import roomescape.exception.custom.status.BadRequestException;

public class ThemeUsedException extends BadRequestException {

    public ThemeUsedException() {
        super("사용중인 테마입니다.");
    }
}
