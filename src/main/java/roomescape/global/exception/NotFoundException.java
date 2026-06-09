package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    public NotFoundException(ErrorCode errorCode) {
        super(HttpStatus.NOT_FOUND, errorCode);
    }

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
