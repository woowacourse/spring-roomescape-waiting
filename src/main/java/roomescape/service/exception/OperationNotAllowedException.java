package roomescape.service.exception;

import roomescape.controller.exception.BaseException;

public class OperationNotAllowedException extends BaseException {

    public OperationNotAllowedException(String detail) {
        super("허용되지 않는 작업입니다.", detail);
    }
}
