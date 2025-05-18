package roomescape.reservationtime.exception;

import roomescape.global.exception.NotFoundException;

public class NotFoundReservationTimeException extends NotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 예약시간이 존재하지 않습니다.";

    public NotFoundReservationTimeException(String message) {
        super(message);
    }

    public NotFoundReservationTimeException() {
        this(DEFAULT_MESSAGE);
    }
}
