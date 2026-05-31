package roomescape.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;

    protected CustomException(final String code, final HttpStatus httpStatus, final String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
