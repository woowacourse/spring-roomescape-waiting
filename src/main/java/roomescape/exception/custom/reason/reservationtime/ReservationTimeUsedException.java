package roomescape.exception.custom.reason.reservationtime;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationTimeUsedException extends BadRequestException {

    public ReservationTimeUsedException() {
        super("예약시간이 사용중입니다.");
    }
}
