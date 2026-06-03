package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;
import roomescape.error.BusinessException;
import roomescape.error.ErrorCode;

public class ForbiddenRequestException extends BusinessException {
    public ForbiddenRequestException() {
        super(HttpStatus.FORBIDDEN, ErrorCode.RESERVATION_FORBIDDEN_REQUEST);
    }
}
