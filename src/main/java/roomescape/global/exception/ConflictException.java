package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    public ConflictException(ErrorCode errorCode) {
        super(HttpStatus.CONFLICT, errorCode);
    }

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
