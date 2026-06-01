package roomescape.global.error.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.error.type.ErrorType;

public class GeneralException extends RuntimeException {

    private final HttpStatus status;

    public GeneralException(ErrorType errorType) {
        super(errorType.message());
        this.status = errorType.status();
    }

    public HttpStatus getStatus() {
        return status;
    }
}
