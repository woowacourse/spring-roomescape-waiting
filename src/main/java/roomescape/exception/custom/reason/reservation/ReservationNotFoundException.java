package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.NotFoundException;

public class ReservationNotFoundException extends NotFoundException {

    public ReservationNotFoundException() {
        super("예약이 존재하지 않습니다.");
    }
}
