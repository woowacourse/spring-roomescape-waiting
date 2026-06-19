package roomescape.exception;

import roomescape.common.BusinessException;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(message);
    }
}
