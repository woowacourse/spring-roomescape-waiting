package roomescape.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends CustomException {

    private static final String MESSAGE = "유효하지 않은 토큰입니다.";
    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public InvalidTokenException() {
        super(MESSAGE, STATUS);
    }
}
