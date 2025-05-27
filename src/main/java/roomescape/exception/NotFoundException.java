package roomescape.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(final String message) {
        super(new ErrorCode(HttpStatus.NOT_FOUND, message));
    }
}
