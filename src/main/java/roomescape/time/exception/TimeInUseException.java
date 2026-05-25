package roomescape.time.exception;

import roomescape.global.exception.DeleteFailedException;

public class TimeInUseException extends DeleteFailedException {

    public TimeInUseException() {
        super("해당 예약 시간에 예약이 존재합니다.");
    }
}
