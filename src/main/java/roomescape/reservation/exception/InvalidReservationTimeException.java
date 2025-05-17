package roomescape.reservation.exception;

import roomescape.global.exception.InvalidInputException;

public class InvalidReservationTimeException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 예약 시간입니다.";

    public InvalidReservationTimeException(String message) {
        super(message);
    }

    public InvalidReservationTimeException() {
        this(DEFAULT_MESSAGE);
    }
}
