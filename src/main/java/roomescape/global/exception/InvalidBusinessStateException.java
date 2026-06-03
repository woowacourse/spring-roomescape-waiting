package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class InvalidBusinessStateException extends BusinessException {
    public InvalidBusinessStateException(ErrorCode errorCode) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, errorCode);
    }
}
