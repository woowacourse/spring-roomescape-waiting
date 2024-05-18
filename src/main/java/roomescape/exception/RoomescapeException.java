package roomescape.exception;

import org.springframework.http.HttpStatus;

public abstract class RoomescapeException extends RuntimeException {

    private final HttpStatus httpStatus;

    protected RoomescapeException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
