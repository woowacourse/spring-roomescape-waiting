package roomescape.reservation.domain.exception;

import roomescape.common.exception.ConflictException;

public class ReservationOptionChangedException extends ConflictException {

    public ReservationOptionChangedException(final Throwable cause) {
        super("예약 가능한 시간 또는 테마 상태가 변경되었습니다.", cause);
    }
}
