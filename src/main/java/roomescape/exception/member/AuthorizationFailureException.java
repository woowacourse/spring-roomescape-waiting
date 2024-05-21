package roomescape.exception.member;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class AuthorizationFailureException extends CustomException {
    public AuthorizationFailureException() {
        super("인가되지 않은 접근입니다.", HttpStatus.FORBIDDEN);
    }
}
