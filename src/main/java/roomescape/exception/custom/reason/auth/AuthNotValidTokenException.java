package roomescape.exception.custom.reason.auth;

import roomescape.exception.custom.status.UnauthorizedException;

public class AuthNotValidTokenException extends UnauthorizedException {

    public AuthNotValidTokenException() {
        super("token이 유효하지 않습니다.");
    }
}
