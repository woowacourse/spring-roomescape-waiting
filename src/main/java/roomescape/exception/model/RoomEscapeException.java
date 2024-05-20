package roomescape.exception.model;

import org.springframework.http.HttpStatus;

public class RoomEscapeException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public RoomEscapeException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }

    public HttpStatus getHttpStatus() {
        return exceptionCode.getHttpStatus();
    }

    public String getMessage() {
        return exceptionCode.getMessage();
    }
}
