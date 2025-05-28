package roomescape.user.exception;

import roomescape.global.exception.ForbiddenException;

public class UserForbiddenException extends ForbiddenException {

    private static final String DEFAULT_MESSAGE = "유저 접근 권한이 없습니다.";

    public UserForbiddenException(String message) {
        super(message);
    }

    public UserForbiddenException() {
        this(DEFAULT_MESSAGE);
    }
}
