package roomescape.exception;

public class InvalidOwnershipException extends RoomescapeException {

    public InvalidOwnershipException() {
        super("INVALID_OWNERSHIP", "본인의 예약만 제어할 수 있습니다.");
    }
}
