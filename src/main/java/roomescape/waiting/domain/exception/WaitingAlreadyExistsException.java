package roomescape.waiting.domain.exception;

import roomescape.common.exception.ConflictException;

public class WaitingAlreadyExistsException extends ConflictException {

    public WaitingAlreadyExistsException(final Throwable cause) {
        super("이미 대기된 슬롯입니다.", cause);
    }
}
