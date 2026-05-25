package roomescape.reservation.exception;

import roomescape.global.exception.BusinessException;

public class AuthorizationException extends BusinessException {

    public AuthorizationException() {
        super("예약 접근 권한이 없습니다.");
    }
}
