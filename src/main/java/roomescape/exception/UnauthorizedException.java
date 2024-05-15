package roomescape.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HttpStatusException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
