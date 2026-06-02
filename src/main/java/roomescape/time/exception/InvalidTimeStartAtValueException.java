package roomescape.time.exception;

import roomescape.global.exception.InvalidRequestValueException;

public class InvalidTimeStartAtValueException extends InvalidRequestValueException {

    public InvalidTimeStartAtValueException() {
        super("시작 시간이 유효하지 않습니다.");
    }
}
