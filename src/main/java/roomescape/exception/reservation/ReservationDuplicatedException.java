package roomescape.exception.reservation;

import roomescape.exception.ConflictException;

public class ReservationDuplicatedException extends ConflictException {

    public ReservationDuplicatedException() {
        super("이미 동일한 명의로 예약 또는 예약 대기가 존재합니다.");
    }
}
