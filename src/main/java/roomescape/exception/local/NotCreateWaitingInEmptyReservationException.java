package roomescape.exception.local;

import roomescape.exception.global.InvalidInputException;

public class NotCreateWaitingInEmptyReservationException extends InvalidInputException {

    private static final String DEFAULT_MESSAGE = "예약이 존재하지 않습니다.";

    public NotCreateWaitingInEmptyReservationException(String message) {
        super(message);
    }

    public NotCreateWaitingInEmptyReservationException() {
        this(DEFAULT_MESSAGE);
    }
}
