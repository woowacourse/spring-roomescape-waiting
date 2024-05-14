package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class ErrorResponse {
    private final HttpStatus statusCode;
    private final String message;

    public ErrorResponse(final HttpStatus statusCode, final String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
