package roomescape.exception.custom.reason.reservationtime;

import roomescape.exception.custom.status.NotFoundException;

public class ReservationTimeNotFoundException extends NotFoundException {

    public ReservationTimeNotFoundException() {
        super("예약시간이 존재하지 않습니다.");
    }
}
