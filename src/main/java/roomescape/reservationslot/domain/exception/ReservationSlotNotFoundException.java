package roomescape.reservationslot.domain.exception;

import roomescape.common.exception.NotFoundException;

public class ReservationSlotNotFoundException extends NotFoundException {

    public ReservationSlotNotFoundException() {
        super("존재하지 않는 예약 슬롯입니다.");
    }
}
