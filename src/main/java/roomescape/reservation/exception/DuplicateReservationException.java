package roomescape.reservation.exception;

import roomescape.global.exception.DuplicateException;

public class DuplicateReservationException extends DuplicateException {

    public DuplicateReservationException() {
        super("예약이 이미 존재합니다.");
    }
}
