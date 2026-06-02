package roomescape.reservation.exception;

import roomescape.global.exception.DuplicateException;

public class ReservationSlotHasWaitingException extends DuplicateException {

    public ReservationSlotHasWaitingException() {
        super("해당 예약 슬롯에 예약 대기가 존재하여 예약할 수 없습니다.");
    }
}
