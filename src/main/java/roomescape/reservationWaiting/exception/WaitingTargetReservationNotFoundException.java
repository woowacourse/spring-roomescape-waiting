package roomescape.reservationWaiting.exception;

import roomescape.global.exception.NotFoundException;

public class WaitingTargetReservationNotFoundException extends NotFoundException {

    public WaitingTargetReservationNotFoundException() {
        super("예약 대기 신청하려는 슬롯의 예약이 존재하지 않습니다.");
    }
}
