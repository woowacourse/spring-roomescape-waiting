package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.ConflictException;

public class ReservationConflictException extends ConflictException {

    public ReservationConflictException() {
        super("이미 존재하는 예약입니다.");
    }
}
