package roomescape.exception.reservation;

import roomescape.exception.NotFoundException;

public class ReservationWaitingNotFoundException extends NotFoundException {

    public ReservationWaitingNotFoundException() {
        super("해당 예약 대기가 존재하지 않습니다.");
    }
}
