package roomescape.exception;

public class PastSlotControlException extends RoomescapeException {

    public PastSlotControlException() {
        super("PAST_SLOT_CONTRL", "이미 지난 예약은 수정/삭제할 수 없습니다.");
    }
}
