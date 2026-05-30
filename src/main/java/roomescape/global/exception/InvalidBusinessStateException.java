package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class InvalidBusinessStateException extends BusinessException {

    public InvalidBusinessStateException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
