package roomescape.exception;

import org.springframework.http.HttpStatus;

public class RoomeescapeException extends RuntimeException {

    private final HttpStatus status;

    public RoomeescapeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
