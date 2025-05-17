package roomescape.user.exception;

import roomescape.global.exception.InvalidInputException;

public class InvalidRoleException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "권한을 찾을 수 없습니다.";

    public InvalidRoleException(String message) {
        super(message);
    }

    public InvalidRoleException() {
        super(DEFAULT_MESSAGE);
    }
}
