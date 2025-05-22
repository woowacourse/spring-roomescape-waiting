package roomescape.exception.custom.reason.schedule;

import roomescape.exception.custom.status.BadRequestException;

public class PastScheduleException extends BadRequestException {

    public PastScheduleException() {
        super("과거에 대한 예약을 할 수 없습니다");
    }
}
