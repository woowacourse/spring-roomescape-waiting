package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends CustomException {

    public ForbiddenException(final String message) {
        super("FORBIDDEN", HttpStatus.FORBIDDEN, message);
    }
}
