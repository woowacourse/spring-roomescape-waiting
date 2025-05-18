package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class PastReservationCreationException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "과거의 예약을 추가할 수 없습니다.";

    public PastReservationCreationException(String message) {
        super(message);
    }

    public PastReservationCreationException() {
        this(DEFAULT_MESSAGE);
    }
}
