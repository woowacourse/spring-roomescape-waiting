package roomescape.exception;

import org.springframework.http.HttpStatus;

public class RoomescapeException extends RuntimeException {
    private final ExceptionType exceptionType;
    private final String logMessage;

    public RoomescapeException(ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.logMessage = exceptionType.getLogMessage();
    }

    public RoomescapeException(ExceptionType exceptionType, Object... resource) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.logMessage = String.format(exceptionType.getLogMessage(), resource);
    }

    @Override
    public String getMessage() {
        return exceptionType.getMessage();
    }

    public HttpStatus getHttpStatus() {
        return exceptionType.getStatus();
    }

    public String getLogMessage() {
        return logMessage;
    }
}
