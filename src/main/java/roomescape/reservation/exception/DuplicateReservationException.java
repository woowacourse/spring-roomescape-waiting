package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;

import roomescape.error.BusinessException;
import roomescape.error.ErrorCode;

public class DuplicateReservationException extends BusinessException {
    public DuplicateReservationException() {
        super(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_RESERVATION);
    }
}
