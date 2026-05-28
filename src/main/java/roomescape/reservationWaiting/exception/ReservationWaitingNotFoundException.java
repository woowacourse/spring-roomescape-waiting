package roomescape.reservationWaiting.exception;

import roomescape.global.exception.NotFoundException;

public class ReservationWaitingNotFoundException extends NotFoundException {

    public ReservationWaitingNotFoundException() {
        super("예약 대기가 존재하지 않습니다.");
    }
}
