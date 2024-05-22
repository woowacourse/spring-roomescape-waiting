package roomescape.exception;

import org.springframework.http.HttpStatus;

public class RoomescapeException extends RuntimeException {
    private final ExceptionType exceptionType;

    public RoomescapeException(ExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public String getMessage() {
        return exceptionType.getMessage();
    }

    public HttpStatus getHttpStatus() {
        return exceptionType.getStatus();
    }
}
