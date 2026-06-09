package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {
    public BadRequestException(ErrorCode errorCode) {
        super(HttpStatus.BAD_REQUEST, errorCode);
    }

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
