package roomescape.exception.reservation;

import roomescape.exception.NotFoundException;

public class WaitingNotFoundException extends NotFoundException {

    public WaitingNotFoundException() {
        super("존재하지 않는 예약 대기 입니다.");
    }
}
