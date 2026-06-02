package roomescape.time.exception;

import roomescape.global.exception.DeleteFailedException;

public class TimeInUseException extends DeleteFailedException {

    public TimeInUseException() {
        super("해당 예약 시간에 예약 또는 예약 대기가 존재합니다.");
    }
}
