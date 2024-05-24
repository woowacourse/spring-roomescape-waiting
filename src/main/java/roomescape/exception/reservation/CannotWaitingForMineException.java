package roomescape.exception.reservation;

import roomescape.exception.ConflictException;

public class CannotWaitingForMineException extends ConflictException {

    public CannotWaitingForMineException() {
        super("자신의 예약에 대해 예약 대기를 할 수 없습니다.");
    }
}
