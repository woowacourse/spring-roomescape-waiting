package roomescape.config.exception;

import roomescape.controller.exception.BaseException;

public class ForbiddenAccessException extends BaseException {

    public ForbiddenAccessException() {
        this("");
    }

    public ForbiddenAccessException(String detail) {
        super("접근 권한이 없습니다.", detail);
    }
}
