package roomescape.reservationWaiting.exception;

import roomescape.global.exception.InvalidRequestValueException;

public class AlreadyReservedSameSlotException extends InvalidRequestValueException {
    public AlreadyReservedSameSlotException() {
        super("이미 동일한 예약 슬롯에 예약이 존재합니다.");
    }
}
