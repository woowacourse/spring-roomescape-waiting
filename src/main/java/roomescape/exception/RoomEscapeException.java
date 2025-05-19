package roomescape.exception;

import org.springframework.http.HttpStatus;

public class RoomEscapeException extends RuntimeException {

    private final HttpStatus httpStatus;

    public RoomEscapeException(HttpStatus httpStatus, ExceptionCause exceptionCause) {
        super(exceptionCause.getMessage());
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
