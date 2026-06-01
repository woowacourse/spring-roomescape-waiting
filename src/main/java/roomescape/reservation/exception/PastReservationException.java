package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;

import roomescape.error.BusinessException;
import roomescape.error.ErrorCode;

public class PastReservationException extends BusinessException {
    private PastReservationException(ErrorCode errorCode) {
        super(HttpStatus.BAD_REQUEST, errorCode);
    }

    public static PastReservationException of(ErrorCode errorCode) {
        return new PastReservationException(errorCode);
    }
}
