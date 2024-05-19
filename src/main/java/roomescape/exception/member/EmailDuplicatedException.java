package roomescape.exception.member;

import roomescape.exception.ConflictException;

public class EmailDuplicatedException extends ConflictException {

    public EmailDuplicatedException() {
        super("이미 가입되어 있는 이메일 주소입니다.");
    }
}
