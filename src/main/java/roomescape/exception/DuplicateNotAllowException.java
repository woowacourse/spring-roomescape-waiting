package roomescape.exception;

import org.springframework.http.HttpStatus;

public class DuplicateNotAllowException extends CustomException {
    public DuplicateNotAllowException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
