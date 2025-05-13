package roomescape.member.exception;

import roomescape.global.exception.AuthorizationException;

public class UnauthorizedUserRoleException extends AuthorizationException {

    private static final String DEFAULT_MESSAGE = "유저가 멤버 권한이 아닙니다.";

    public UnauthorizedUserRoleException(String message) {
        super(message);
    }

    public UnauthorizedUserRoleException() {
        this(DEFAULT_MESSAGE);
    }
}
