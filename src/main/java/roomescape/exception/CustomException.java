package roomescape.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String logMessage;

    public CustomException(HttpStatus httpStatus, String message) {
        this(httpStatus, message, message);
    }

    public CustomException(HttpStatus httpStatus, String logMessage, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.logMessage = logMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getLogMessage() {
        return logMessage;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getLogMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
