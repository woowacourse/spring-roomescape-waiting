package roomescape.exception.custom.reason.auth;

import roomescape.exception.custom.status.BadRequestException;

public class AuthNotValidPasswordException extends BadRequestException {

    public AuthNotValidPasswordException() {
        super("잘못된 비밀번호입니다.");
    }
}
