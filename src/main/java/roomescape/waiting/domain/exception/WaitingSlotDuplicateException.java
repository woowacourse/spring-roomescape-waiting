package roomescape.waiting.domain.exception;

import roomescape.common.exception.ConflictException;

public class WaitingSlotDuplicateException extends ConflictException {

    public WaitingSlotDuplicateException() {
        super("해당 슬롯의 대기가 이미 존재합니다.");
    }
}
