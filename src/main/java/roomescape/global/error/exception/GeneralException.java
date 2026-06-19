package roomescape.global.error.exception;

import org.springframework.http.HttpStatus;
import roomescape.global.error.type.ErrorType;

public class GeneralException extends RuntimeException {

    private final ErrorType errorType;

    public GeneralException(ErrorType errorType) {
        super(errorType.message());
        this.errorType = errorType;
    }

    public HttpStatus getStatus() {
        return errorType.status();
    }

    public String getCode() {
        return errorType.code();
    }
}
