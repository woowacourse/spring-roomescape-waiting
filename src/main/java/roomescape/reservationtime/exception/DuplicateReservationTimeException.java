package roomescape.reservationtime.exception;

import roomescape.global.exception.ConflictException;

public class DuplicateReservationTimeException extends ConflictException {

    private static final String DEFAULT_MESSAGE = "이미 등록되어 있는 예약 시간입니다.";

    public DuplicateReservationTimeException(String message) {
        super(message);
    }

    public DuplicateReservationTimeException() {
        this(DEFAULT_MESSAGE);
    }
}
