package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(HttpStatus.UNAUTHORIZED, errorCode);
    }
}
