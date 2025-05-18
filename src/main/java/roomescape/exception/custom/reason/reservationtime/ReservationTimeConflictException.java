package roomescape.exception.custom.reason.reservationtime;

import roomescape.exception.custom.status.ConflictException;

public class ReservationTimeConflictException extends ConflictException {

    public ReservationTimeConflictException() {
        super("예약시간이 이미 존재합니다.");
    }
}
