package roomescape.reservationWaiting.exception;

import roomescape.global.exception.InvalidRequestValueException;

public class AlreadyReservedException extends InvalidRequestValueException {
    public AlreadyReservedException() {
        super("이미 예약을 하였습니다.");
    }
}
