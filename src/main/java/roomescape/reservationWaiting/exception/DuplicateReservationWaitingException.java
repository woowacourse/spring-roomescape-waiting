package roomescape.reservationWaiting.exception;

import roomescape.global.exception.BusinessException;

public class DuplicateReservationWaitingException extends BusinessException {

    public DuplicateReservationWaitingException() {
        super("기존에 이미 예약 대기가 존재합니다.");
    }
}
