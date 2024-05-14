package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ErrorResponse {
    private final HttpStatus httpStatus;
    private final String path;
    private final String message;

    public ErrorResponse(final HttpStatus httpStatus, final String path, final String message) {
        this.httpStatus = httpStatus;
        this.path = path;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }
}
