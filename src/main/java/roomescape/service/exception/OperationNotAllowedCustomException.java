package roomescape.service.exception;

import org.springframework.http.HttpStatus;
import roomescape.controller.exception.BaseCustomException;

public class OperationNotAllowedCustomException extends BaseCustomException {

    public OperationNotAllowedCustomException(String detail) {
        super(HttpStatus.BAD_REQUEST, "허용되지 않는 작업입니다.", detail);
    }
}
