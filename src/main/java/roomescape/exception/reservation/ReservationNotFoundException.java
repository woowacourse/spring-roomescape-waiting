package roomescape.exception.reservation;

import roomescape.exception.NotFoundException;

public class ReservationNotFoundException extends NotFoundException {

    public ReservationNotFoundException() {
        super("해당 예약이 존재하지 않습니다.");
    }
}
