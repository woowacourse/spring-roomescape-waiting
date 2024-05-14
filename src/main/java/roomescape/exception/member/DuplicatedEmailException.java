package roomescape.exception.member;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class DuplicatedEmailException extends CustomException {
    public DuplicatedEmailException() {
        super("중복된 이메일입니다.", HttpStatus.BAD_REQUEST);
    }
}
