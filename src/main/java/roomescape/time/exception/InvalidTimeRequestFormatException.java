package roomescape.time.exception;

import roomescape.global.exception.InvalidRequestFormatException;

public class InvalidTimeRequestFormatException extends InvalidRequestFormatException {

    public InvalidTimeRequestFormatException() {
        super("예약 시간 요청 형식이 유효하지 않습니다.");
    }
}
