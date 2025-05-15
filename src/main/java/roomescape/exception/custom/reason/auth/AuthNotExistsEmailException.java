package roomescape.exception.custom.reason.auth;

import roomescape.exception.custom.status.BadRequestException;

public class AuthNotExistsEmailException extends BadRequestException {

    public AuthNotExistsEmailException() {
        super("존재하지 않는 이메일입니다.");
    }
}
