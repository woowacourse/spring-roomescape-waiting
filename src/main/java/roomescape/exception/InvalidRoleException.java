package roomescape.exception;

import org.springframework.http.HttpStatus;

public class InvalidRoleException extends CustomException {

    private static final String MESSAGE = "잘못된 role입니다.";
    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public InvalidRoleException() {
        super(MESSAGE, STATUS);
    }
}
