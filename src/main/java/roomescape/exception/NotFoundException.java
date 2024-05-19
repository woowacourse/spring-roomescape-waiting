package roomescape.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RoomeescapeException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
