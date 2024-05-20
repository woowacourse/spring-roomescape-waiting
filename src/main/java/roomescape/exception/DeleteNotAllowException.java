package roomescape.exception;

import org.springframework.http.HttpStatus;

public class DeleteNotAllowException extends CustomException {
    public DeleteNotAllowException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
