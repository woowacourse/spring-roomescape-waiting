package roomescape.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException(final String message) {
        super("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, message);
    }
}
