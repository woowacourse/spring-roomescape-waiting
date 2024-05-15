package roomescape.exception.reservation;

import roomescape.exception.ConflictException;

public class TimeDuplicatedException extends ConflictException {

    public TimeDuplicatedException() {
        super("중복된 시간을 입력할 수 없습니다.");
    }
}
