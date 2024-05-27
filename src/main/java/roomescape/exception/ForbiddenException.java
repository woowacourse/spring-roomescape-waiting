package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends RoomeescapeException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
