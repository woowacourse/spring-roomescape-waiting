package roomescape.dto.request.exception;

import org.springframework.http.HttpStatus;
import roomescape.controller.exception.BaseCustomException;

public class InputNotAllowedCustomException extends BaseCustomException {

    public InputNotAllowedCustomException(String detail) {
        super(HttpStatus.BAD_REQUEST, "입력 형식이 올바르지 않습니다.", detail);
    }
}
