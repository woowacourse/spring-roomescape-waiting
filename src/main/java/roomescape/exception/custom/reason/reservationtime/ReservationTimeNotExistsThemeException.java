package roomescape.exception.custom.reason.reservationtime;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationTimeNotExistsThemeException extends BadRequestException {

    public ReservationTimeNotExistsThemeException() {
        super("존재하지 않는 theme입니다.");
    }
}
