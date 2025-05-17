package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ExpiredTokenException extends CustomException {

    private static final String MESSAGE = "토큰이 만료되었습니다.";
    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public ExpiredTokenException() {
        super(MESSAGE, STATUS);
    }
}
