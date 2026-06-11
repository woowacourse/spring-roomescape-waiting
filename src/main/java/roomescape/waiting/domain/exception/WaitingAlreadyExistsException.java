package roomescape.waiting.domain.exception;

import roomescape.common.exception.ConflictException;

public class WaitingAlreadyExistsException extends ConflictException {

    public WaitingAlreadyExistsException() {
        super("이미 대기가 있는 슬롯입니다.");
    }
}
