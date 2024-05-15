package roomescape.exception.reservation;

import roomescape.exception.BadRequestException;

public class TimeUsingException extends BadRequestException {

    public TimeUsingException() {
        super("해당 시간에 예약이 있어 삭제할 수 없습니다.");
    }
}
