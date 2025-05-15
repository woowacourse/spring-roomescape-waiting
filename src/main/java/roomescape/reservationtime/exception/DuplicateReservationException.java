package roomescape.reservationtime.exception;

import roomescape.global.exception.ConflictException;

public class DuplicateReservationException extends ConflictException {

    private static final String DEFAULT_MESSAGE = "이미 등록되어 있는 예약 시간입니다.";

    public DuplicateReservationException(String message) {
        super(message);
    }

    public DuplicateReservationException() {
        this(DEFAULT_MESSAGE);
    }
}
