package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationPastDateException extends BadRequestException {
    public ReservationPastDateException() {
        super("과거 날짜로 예약할 수 없습니다.");
    }
}
