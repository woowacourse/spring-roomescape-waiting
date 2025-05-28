package roomescape.user.exception;

import roomescape.global.exception.NotFoundException;
import roomescape.user.domain.Role;

public class NotFoundUserException extends NotFoundException {

    private static final String DEFAULT_ROLE_FIELD = "유저";
    private static final String DEFAULT_MESSAGE = "해당 %s를 찾을 수 없습니다";

    public NotFoundUserException(String message) {
        super(message);
    }

    public NotFoundUserException() {
        this(String.format(DEFAULT_MESSAGE, DEFAULT_ROLE_FIELD));
    }

    protected NotFoundUserException(Role role) {
        this(String.format(DEFAULT_MESSAGE, role.getKoreanName()));
    }

}
