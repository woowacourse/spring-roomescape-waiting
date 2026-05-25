package roomescape.time.exception;

import roomescape.global.exception.InvalidRequestValueException;

public class InvalidTimeRequestValueException extends InvalidRequestValueException {

    public InvalidTimeRequestValueException() {
        super("예약 시간 요청 값이 유효하지 않습니다.");
    }
}
