package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateException extends CustomException {

    public DuplicateException(final String message) {
        super("DUPLICATE", HttpStatus.CONFLICT, message);
    }
}
