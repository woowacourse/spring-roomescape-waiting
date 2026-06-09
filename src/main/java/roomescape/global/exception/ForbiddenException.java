package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(ErrorCode errorCode) {
        super(HttpStatus.FORBIDDEN, errorCode);
    }

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
