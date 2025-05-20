package roomescape.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RoomEscapeException {

    public NotFoundException(ExceptionCause exceptionCause) {
        super(HttpStatus.NOT_FOUND, exceptionCause);
    }
}
