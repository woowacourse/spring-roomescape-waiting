package roomescape.reservation.domain.exception;

import roomescape.common.exception.NotFoundException;

public class ReservationNotOwnedException extends NotFoundException {

    public ReservationNotOwnedException() {
        super("존재하지 않는 예약입니다.");
    }
}
