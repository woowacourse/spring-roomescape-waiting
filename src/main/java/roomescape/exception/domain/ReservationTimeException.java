package roomescape.exception.domain;

import roomescape.exception.RoomescapeException;
import roomescape.exception.code.ReservationTimeErrorCode;

public class ReservationTimeException extends RoomescapeException {

    public ReservationTimeException(ReservationTimeErrorCode reservationTimeErrorCode) {
        super(reservationTimeErrorCode);
    }
}
