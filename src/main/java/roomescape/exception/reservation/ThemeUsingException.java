package roomescape.exception.reservation;

import roomescape.exception.BadRequestException;

public class ThemeUsingException extends BadRequestException {

    public ThemeUsingException() {
        super("해당 테마에 예약이 있어 삭제할 수 없습니다.");
    }
}
