package roomescape.exception;

import org.springframework.http.HttpStatus;

public abstract class RoomEscapeException extends RuntimeException {
    private final HttpStatus httpStatus;

    protected RoomEscapeException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    protected HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
