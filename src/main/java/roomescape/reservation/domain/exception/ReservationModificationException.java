package roomescape.reservation.domain.exception;

import roomescape.common.exception.ConflictException;

public class ReservationModificationException extends ConflictException {

    public ReservationModificationException() {
        super("당일 예약은 변경할 수 없습니다.");
    }
}
