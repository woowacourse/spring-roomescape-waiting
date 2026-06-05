package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;

import roomescape.global.error.BusinessException;
import roomescape.global.error.ErrorCode;

public class PastReservationException extends BusinessException {
    public PastReservationException(ErrorCode errorCode) {
        super(HttpStatus.BAD_REQUEST, errorCode);
    }
}
