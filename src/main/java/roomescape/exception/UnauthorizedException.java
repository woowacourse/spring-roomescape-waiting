package roomescape.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RoomEscapeException {

    public UnauthorizedException(ExceptionCause exceptionCause) {
        super(HttpStatus.UNAUTHORIZED, exceptionCause);
    }
}
