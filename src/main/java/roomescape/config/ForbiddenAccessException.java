package roomescape.config;

import roomescape.controller.exception.BaseException;

public class ForbiddenAccessException extends BaseException {

    public ForbiddenAccessException() {
        super("접근 권한이 없습니다.", "");
    }

    public ForbiddenAccessException(String detail) {
        super("접근 권한이 없습니다.", detail);
    }
}
