package roomescape.exception.custom.reason.auth;

import roomescape.exception.custom.status.BadRequestException;

public class AuthNotExistsCookieException extends BadRequestException {

    public AuthNotExistsCookieException() {
        super("쿠키가 존재하지 않습니다.");
    }
}
