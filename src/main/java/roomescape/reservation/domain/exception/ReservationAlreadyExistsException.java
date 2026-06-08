package roomescape.reservation.domain.exception;

import roomescape.common.exception.ConflictException;

public class ReservationAlreadyExistsException extends ConflictException {

    public ReservationAlreadyExistsException() {
        super("이미 예약이 있는 슬롯입니다.");
    }
}
