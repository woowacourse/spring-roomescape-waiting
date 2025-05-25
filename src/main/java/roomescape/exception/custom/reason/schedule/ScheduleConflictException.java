package roomescape.exception.custom.reason.schedule;

import roomescape.exception.custom.status.ConflictException;

public class ScheduleConflictException extends ConflictException {
    
    public ScheduleConflictException() {
        super("이미 존재하는 스케줄입니다.");
    }
}
