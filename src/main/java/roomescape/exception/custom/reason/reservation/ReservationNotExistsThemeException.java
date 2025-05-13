package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationNotExistsThemeException extends BadRequestException {

    public ReservationNotExistsThemeException() {
        super("예약하려는 테마가 존재하지 않습니다.");
    }
}
