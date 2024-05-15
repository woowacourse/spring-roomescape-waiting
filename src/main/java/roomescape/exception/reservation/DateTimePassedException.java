package roomescape.exception.reservation;

import roomescape.exception.BadRequestException;

public class DateTimePassedException extends BadRequestException {

    public DateTimePassedException() {
        super("지나간 날짜와 시간에 대한 예약은 불가능합니다.");
    }
}
