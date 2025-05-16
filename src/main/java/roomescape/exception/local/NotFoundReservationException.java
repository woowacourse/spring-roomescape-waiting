package roomescape.exception.local;

import roomescape.exception.global.NotFoundException;

public class NotFoundReservationException extends NotFoundException {

    private static final String DEFAULT_MESSAGE = "예약을 찾을 수 없습니다.";

    public NotFoundReservationException(String message) {
        super(message);
    }

    public NotFoundReservationException() {
        this(DEFAULT_MESSAGE);
    }
}
