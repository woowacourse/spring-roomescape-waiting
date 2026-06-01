package roomescape.exception;

public class TimeSlotNotFoundException extends RoomescapeException {

    public TimeSlotNotFoundException() {
        super("TIME_SLOT_NOT_FOUND", "해당 시간대를 찾을 수 없습니다.");
    }
}
