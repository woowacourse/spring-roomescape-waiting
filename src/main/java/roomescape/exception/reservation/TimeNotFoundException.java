package roomescape.exception.reservation;

import roomescape.exception.NotFoundException;

public class TimeNotFoundException extends NotFoundException {

    public TimeNotFoundException() {
        super("예약 하려는 시간이 저장되어 있지 않습니다.");
    }
}
