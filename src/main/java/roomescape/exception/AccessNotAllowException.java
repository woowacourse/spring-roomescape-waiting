package roomescape.exception;

import org.springframework.http.HttpStatus;

public class AccessNotAllowException extends CustomException {
    public AccessNotAllowException(final String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}

