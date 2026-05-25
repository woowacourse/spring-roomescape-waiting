package roomescape.reservation.exception;

import roomescape.global.exception.BusinessException;

public class ForbiddenException extends BusinessException {

    public ForbiddenException() {
        super("예약 접근 권한이 없습니다.");
    }
}
