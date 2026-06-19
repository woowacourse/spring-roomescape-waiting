package roomescape.exception;

import roomescape.common.BusinessException;

public class DuplicateEntityException extends BusinessException {

    public DuplicateEntityException(String message, Object... args) {
        super(message.formatted(args));
    }
}
