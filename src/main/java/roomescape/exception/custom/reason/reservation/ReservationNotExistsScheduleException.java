package roomescape.exception.custom.reason.reservation;

import roomescape.exception.custom.status.BadRequestException;

public class ReservationNotExistsScheduleException extends BadRequestException {

    public ReservationNotExistsScheduleException() {
        super("스케줄에 대한 예약이 존재하지 않습니다.");
    }
}
