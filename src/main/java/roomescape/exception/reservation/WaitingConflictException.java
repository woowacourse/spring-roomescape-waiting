package roomescape.exception.reservation;

import roomescape.exception.ConflictException;

public class WaitingConflictException extends ConflictException {

    public WaitingConflictException() {
        super("같은 시간에 이미 동일한 예약 대기가 존재합니다.");
    }
}
