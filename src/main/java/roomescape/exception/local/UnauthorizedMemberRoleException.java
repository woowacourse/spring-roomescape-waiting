package roomescape.exception.local;

import roomescape.exception.global.AuthorizationException;

public class UnauthorizedMemberRoleException extends AuthorizationException {

    private static final String DEFAULT_MESSAGE = "유저가 멤버 권한이 아닙니다.";

    public UnauthorizedMemberRoleException(String message) {
        super(message);
    }

    public UnauthorizedMemberRoleException() {
        this(DEFAULT_MESSAGE);
    }
}
