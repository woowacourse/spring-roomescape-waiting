package roomescape.service.exception;

import org.springframework.http.HttpStatus;
import roomescape.controller.exception.BaseCustomException;

public class UnauthorizedOperationException extends BaseCustomException {

    public UnauthorizedOperationException(String detail) {
        super(HttpStatus.FORBIDDEN, "작업을 수행할 권한이 없습니다.", detail);
    }
}
