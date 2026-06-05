package roomescape.reservation.domain.exception;

import roomescape.common.exception.ConflictException;

public class ReservationCancellationException extends ConflictException {

    public ReservationCancellationException() {
        super("당일 예약은 취소할 수 없습니다.");
    }
}
