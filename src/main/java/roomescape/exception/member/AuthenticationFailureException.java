package roomescape.exception.member;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class AuthenticationFailureException extends CustomException {
    public AuthenticationFailureException() {
        super("인증에 실패하였습니다", HttpStatus.UNAUTHORIZED);
    }
}
