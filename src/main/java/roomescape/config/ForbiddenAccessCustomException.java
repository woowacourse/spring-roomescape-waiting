package roomescape.config;

import org.springframework.http.HttpStatus;
import roomescape.controller.exception.BaseCustomException;

public class ForbiddenAccessCustomException extends BaseCustomException {

    public ForbiddenAccessCustomException() {
        super(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", "");
    }
}
