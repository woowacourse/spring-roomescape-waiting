package roomescape.exception.custom.reason.schedule;

import roomescape.exception.custom.status.BadRequestException;

public class ScheduleNotExistException extends BadRequestException {

    public ScheduleNotExistException() {
        super("스케줄이 존재하지 않습니다.");
    }
}
