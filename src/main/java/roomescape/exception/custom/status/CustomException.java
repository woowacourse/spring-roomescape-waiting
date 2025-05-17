package roomescape.exception.custom.status;

import org.springframework.http.HttpStatus;

public abstract class CustomException extends RuntimeException {

    private final HttpStatus status;

    public CustomException(HttpStatus errorCode, String message) {
        super(message);
        this.status = errorCode;
    }

    public int getStatusValue() {
        return status.value();
    }
}
