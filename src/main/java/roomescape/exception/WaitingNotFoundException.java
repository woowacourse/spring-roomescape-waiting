package roomescape.exception;

import roomescape.service.dto.WaitingCommand;

public class WaitingNotFoundException extends RoomescapeException {

    public WaitingNotFoundException(WaitingCommand waiting) {
        super("WAITING_NOT_FOUND", "해당 식별자의 예약 대기를 찾을 수 없습니다. " + waiting.toString());
    }
}
