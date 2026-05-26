package roomescape.reservation.domain.exception;

import roomescape.common.exception.ConflictException;

public class ReservationAlreadyExistsException extends ConflictException {

    public ReservationAlreadyExistsException(final Throwable cause) {
        super("이미 예약된 시간입니다.", cause);
    }
}
