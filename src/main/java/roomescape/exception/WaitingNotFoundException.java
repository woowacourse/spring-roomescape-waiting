package roomescape.exception;

public class WaitingNotFoundException extends RoomescapeException {

    public WaitingNotFoundException() {
        super("WAITING_NOT_FOUND", "해당하는 예약 대기 정보를 찾을 수 없습니다.");
    }
}
