package roomescape.member.exception;

import roomescape.user.domain.Role;
import roomescape.user.exception.NotFoundUserException;

public class NotFoundMemberException extends NotFoundUserException {

    private static final Role DEFAULT_ROLE_FIELD = Role.ROLE_MEMBER;

    public NotFoundMemberException(String message) {
        super(message);
    }

    public NotFoundMemberException() {
        super(DEFAULT_ROLE_FIELD);
    }
}
