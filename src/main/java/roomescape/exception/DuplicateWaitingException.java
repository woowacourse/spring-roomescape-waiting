package roomescape.exception;

import roomescape.service.dto.WaitingCommand;

public class DuplicateWaitingException extends RoomescapeException {

    public DuplicateWaitingException(WaitingCommand waiting) {
        super("DUPLICATE_WAITING", "이미 등록된 예약 대기입니다. (" + waiting.toString() + ")");
    }
}
