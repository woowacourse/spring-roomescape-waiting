package roomescape.reservationtime.domain.exception;

import roomescape.common.exception.NotFoundException;

public class ReservationTimeNotFoundException extends NotFoundException {

    public ReservationTimeNotFoundException() {
        super("존재하지 않는 예약 시간입니다.");
    }
}
