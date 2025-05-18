package roomescape.reservationtime.exception;

import roomescape.global.exception.InvalidInputException;

public class AlreadyReservedTimeException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "예약에서 사용 중인 시간입니다.";

    public AlreadyReservedTimeException(String message) {
        super(message);
    }

    public AlreadyReservedTimeException() {
        this(DEFAULT_MESSAGE);
    }
}
