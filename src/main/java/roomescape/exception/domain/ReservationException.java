package roomescape.exception.domain;


import roomescape.exception.RoomescapeException;
import roomescape.exception.code.ReservationErrorCode;

public class ReservationException extends RoomescapeException {

    public ReservationException(ReservationErrorCode reservationErrorCode) {
        super(reservationErrorCode);
    }
}
