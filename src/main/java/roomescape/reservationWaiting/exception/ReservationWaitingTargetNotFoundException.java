package roomescape.reservationWaiting.exception;

import roomescape.global.exception.NotFoundException;

public class ReservationWaitingTargetNotFoundException extends NotFoundException {

    public ReservationWaitingTargetNotFoundException() {
        super("예약 대기는 이미 예약된 슬롯에만 신청할 수 있습니다.");
    }
}
