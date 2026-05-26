package roomescape.reservationtime.domain.exception;

import roomescape.common.exception.ConflictException;

public class ReservationTimeInUseException extends ConflictException {

    public ReservationTimeInUseException(final Throwable cause) {
        super("해당 시간에 예약이 존재하여 삭제할 수 없습니다.", cause);
    }
}
