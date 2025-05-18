package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationPastTimeException extends BadRequestException {
    public ReservationPastTimeException() {
        super("과거 시간으로 예약할 수 없습니다.");
    }
}
