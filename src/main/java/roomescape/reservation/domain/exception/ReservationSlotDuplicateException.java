package roomescape.reservation.domain.exception;

import roomescape.common.exception.ConflictException;

public class ReservationSlotDuplicateException extends ConflictException {

    public ReservationSlotDuplicateException() {
        super("해당 슬롯의 예약이 이미 존재합니다.");
    }
}
