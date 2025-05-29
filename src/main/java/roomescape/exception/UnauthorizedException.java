package roomescape.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(final String message) {
        super(new ErrorCode(HttpStatus.UNAUTHORIZED, message));
    }
}
