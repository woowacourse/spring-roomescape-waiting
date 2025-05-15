package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationNotExistsMemberException extends BadRequestException {

    public ReservationNotExistsMemberException() {
        super("존재하지 않는 member입니다.");
    }
}
