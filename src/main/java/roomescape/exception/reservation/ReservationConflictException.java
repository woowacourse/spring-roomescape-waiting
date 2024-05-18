package roomescape.exception.reservation;

import roomescape.exception.ConflictException;

public class ReservationConflictException extends ConflictException {

    public ReservationConflictException() {
        super("해당 테마는 같은 시간에 이미 예약이 존재합니다.");
    }
}
