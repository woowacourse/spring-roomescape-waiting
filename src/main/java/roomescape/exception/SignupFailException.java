package roomescape.exception;

import org.springframework.http.HttpStatus;

public class SignupFailException extends CustomException {
    public SignupFailException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
