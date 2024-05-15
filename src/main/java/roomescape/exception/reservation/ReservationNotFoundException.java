package roomescape.exception.reservation;

import roomescape.exception.NotFoundException;

public class ReservationNotFoundException extends NotFoundException {

    public ReservationNotFoundException() {
        super("존재하지 않는 아이디입니다.");
    }
}
