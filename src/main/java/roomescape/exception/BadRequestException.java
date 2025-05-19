package roomescape.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RoomEscapeException {

    public BadRequestException(ExceptionCause exceptionCause) {
        super(HttpStatus.BAD_REQUEST, exceptionCause);
    }
}
