package roomescape.theme.exception;

import roomescape.global.exception.DuplicateException;

public class DuplicateThemeException extends DuplicateException {

    public DuplicateThemeException() {
        super("테마가 이미 존재합니다.");
    }
}
