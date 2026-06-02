package roomescape.time.exception;

import roomescape.global.exception.NotFoundException;

public class TimeNotFoundException extends NotFoundException {

    public TimeNotFoundException() {
        super("예약 시간이 존재하지 않습니다.");
    }
}
