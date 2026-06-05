package roomescape.wating.domain.exception;

import roomescape.common.exception.ConflictException;

public class WaitingSlotDuplicateException extends ConflictException {

    public WaitingSlotDuplicateException() {
        super("해당 시간에 이미 대기가 존재합니다.");
    }
}
