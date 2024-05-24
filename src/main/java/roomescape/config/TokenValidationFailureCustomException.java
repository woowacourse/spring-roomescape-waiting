package roomescape.config;

import org.springframework.http.HttpStatus;
import roomescape.controller.exception.BaseCustomException;

public class TokenValidationFailureCustomException extends BaseCustomException {

    public TokenValidationFailureCustomException(String detail) {
        super(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다.", detail);
    }
}
