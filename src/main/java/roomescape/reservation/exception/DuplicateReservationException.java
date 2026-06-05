package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;

import roomescape.global.error.BusinessException;
import roomescape.global.error.ErrorCode;

public class DuplicateReservationException extends BusinessException {
    public DuplicateReservationException() {
        super(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_RESERVATION);
    }
}
