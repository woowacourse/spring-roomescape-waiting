package roomescape.exception;

public class PastSessionControlException extends RoomescapeException {

    public PastSessionControlException() {
        super("PAST_SESSION_CONTROL", "이미 지난 예약은 수정/삭제할 수 없습니다.");
    }
}
