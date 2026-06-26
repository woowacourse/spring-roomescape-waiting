package roomescape.exception;

public class InvalidWaitingPrerequisiteException extends RoomescapeException {

    public InvalidWaitingPrerequisiteException() {
        super("INVALID_WAITING_PREREQUISITE", "존재하는 예약에만 대기를 신청할 수 있습니다.");
    }
}
