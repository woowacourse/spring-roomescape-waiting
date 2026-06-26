package roomescape.exception;

public class DuplicateTimeException extends RoomescapeException {

    public DuplicateTimeException(String time) {
        super("DUPLICATE_TIME_SLOT", "이미 등록된 예약대 시간입니다. (" + time + ")");
    }
}
