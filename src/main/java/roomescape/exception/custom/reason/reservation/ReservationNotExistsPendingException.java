package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationNotExistsPendingException extends BadRequestException {

    public ReservationNotExistsPendingException() {
        super("예약된 예약이 없습니다.");
    }
}
