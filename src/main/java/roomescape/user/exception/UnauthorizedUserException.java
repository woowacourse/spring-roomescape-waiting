package roomescape.user.exception;

import roomescape.global.exception.AuthorizationException;
import roomescape.user.domain.Role;

public class UnauthorizedUserException extends AuthorizationException {

    private static final String DEFAULT_ROLE_FILED = "유저";
    private static final String DEFAULT_MESSAGE = "%s의 권한이 아닙니다.";

    public UnauthorizedUserException(String message) {
        super(message);
    }

    public UnauthorizedUserException() {
        this(String.format(DEFAULT_MESSAGE, DEFAULT_ROLE_FILED));
    }

    protected UnauthorizedUserException(Role role) {
        this(String.format(DEFAULT_MESSAGE, role.getKoreanName()));
    }
}
