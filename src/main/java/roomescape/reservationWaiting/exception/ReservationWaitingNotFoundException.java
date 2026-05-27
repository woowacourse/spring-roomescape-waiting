package roomescape.reservationWaiting.exception;

import roomescape.global.exception.NotFoundException;

public class ReservationWaitingNotFoundException extends NotFoundException {
    public ReservationWaitingNotFoundException() {
        super("삭제 대상 예약 대기를 찾을 수 없습니다");
    }
}
