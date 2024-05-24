package roomescape.service.exception;

import org.springframework.http.HttpStatus;
import roomescape.controller.exception.BaseCustomException;

public class ResourceNotFoundCustomException extends BaseCustomException {

    public ResourceNotFoundCustomException(String detail) {
        super(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.", detail);
    }
}
