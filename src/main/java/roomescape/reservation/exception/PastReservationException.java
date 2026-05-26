package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;

import roomescape.error.BusinessException;
import roomescape.error.ErrorCode;

public class PastReservationException extends BusinessException {
    private PastReservationException(ErrorCode errorCode) {
        super(HttpStatus.BAD_REQUEST, errorCode);
    }

    public static PastReservationException pastReservation() {
        return new PastReservationException(ErrorCode.PAST_RESERVATION_CREATE);
    }

    public static PastReservationException pastCancel() {
        return new PastReservationException(ErrorCode.PAST_RESERVATION_CANCEL);
    }

    public static PastReservationException pastUpdate() {
        return new PastReservationException(ErrorCode.PAST_RESERVATION_UPDATE);
    }
}
