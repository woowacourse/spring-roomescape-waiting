package roomescape.member.exception;

import roomescape.user.domain.Role;
import roomescape.user.exception.UnauthorizedUserException;

public class UnauthorizedMemberException extends UnauthorizedUserException {

    private static final Role DEFAULT_ROLE_FILED = Role.ROLE_MEMBER;

    public UnauthorizedMemberException(String message) {
        super(message);
    }

    public UnauthorizedMemberException() {
        super(DEFAULT_ROLE_FILED);
    }
}
