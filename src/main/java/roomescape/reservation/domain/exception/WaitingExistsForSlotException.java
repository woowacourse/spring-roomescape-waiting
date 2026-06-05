package roomescape.reservation.domain.exception;

import roomescape.common.exception.ConflictException;

public class WaitingExistsForSlotException extends ConflictException {

    public WaitingExistsForSlotException() {
        super("대기가 존재하는 슬롯에 예약할 수 없습니다.");
    }
}
