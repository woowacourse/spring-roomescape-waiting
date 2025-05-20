package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends RoomEscapeException {

    public ConflictException(ExceptionCause exceptionCause) {
        super(HttpStatus.CONFLICT, exceptionCause);
    }
}
