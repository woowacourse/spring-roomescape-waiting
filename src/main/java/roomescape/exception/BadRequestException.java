package roomescape.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {

    public BadRequestException(final String message) {
        super(new ErrorCode(HttpStatus.BAD_REQUEST, message));
    }
}
