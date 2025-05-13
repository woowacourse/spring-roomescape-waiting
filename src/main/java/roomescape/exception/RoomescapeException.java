package roomescape.exception;

import org.springframework.http.HttpStatus;

public abstract class RoomescapeException extends RuntimeException {
    private final HttpStatus httpStatus;

    protected RoomescapeException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    protected HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
