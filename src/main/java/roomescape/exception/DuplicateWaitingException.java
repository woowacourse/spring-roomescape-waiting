package roomescape.exception;

import roomescape.domain.Waiting;

public class DuplicateWaitingException extends RoomescapeException {

    public DuplicateWaitingException() {
        super("DUPLICATE_WAITING", "해당 날짜의 시간과 테마는 이미 예약 대기되어 있습니다.");
    }
}
