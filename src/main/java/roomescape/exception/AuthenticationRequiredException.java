package roomescape.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationRequiredException extends CustomException {

    private static final String MESSAGE = "로그인이 필요합니다.";
    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    public AuthenticationRequiredException() {
        super(MESSAGE, STATUS);
    }
}
