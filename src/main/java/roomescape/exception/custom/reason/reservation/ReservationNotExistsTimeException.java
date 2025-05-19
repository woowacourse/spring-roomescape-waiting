package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationNotExistsTimeException extends BadRequestException {

    public ReservationNotExistsTimeException() {
        super("예약하려는 시간이 존재하지 않습니다.");
    }
}
