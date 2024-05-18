package roomescape.domain.member.exception;

import roomescape.global.exception.EscapeApplicationException;

public class InvalidEmailPasswordException extends EscapeApplicationException {
    public InvalidEmailPasswordException() {
        super("이메일 또는 비밀번호를 잘못 입력했습니다.");
    }
}
