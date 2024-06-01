package roomescape.service.exception;

import org.springframework.http.HttpStatus;
import roomescape.controller.exception.BaseCustomException;

public class AuthFailureCustomException extends BaseCustomException {

    public AuthFailureCustomException(String detail) {
        super(HttpStatus.UNAUTHORIZED, "인증이 실패했습니다.", detail);
    }
}
