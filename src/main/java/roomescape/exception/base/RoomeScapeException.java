package roomescape.exception.base;

import org.springframework.http.HttpStatus;

public abstract class RoomeScapeException extends RuntimeException {

    private final HttpStatus status;

    protected RoomeScapeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
