package roomescape.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {

    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public HttpStatus getStatus() {
        return status;
    }
}
