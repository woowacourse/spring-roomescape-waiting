package roomescape.reservationWaiting.exception;

import roomescape.global.exception.DuplicateException;

public class DuplicateReservationWaitingException extends DuplicateException {

    public DuplicateReservationWaitingException() {
        super("예약 대기가 이미 존재합니다.");
    }
}
