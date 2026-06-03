package roomescape.exception;

public class NotOwnerException extends RoomescapeException {

    public NotOwnerException() {
        super("NOT_OWNER", "본인의 예약만 제어할 수 있습니다.");
    }
}
