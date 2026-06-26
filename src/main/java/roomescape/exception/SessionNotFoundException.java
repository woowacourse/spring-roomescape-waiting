package roomescape.exception;

public class SessionNotFoundException extends RoomescapeException {

    public SessionNotFoundException() {
        super("SESSION_NOT_FOUND", "해당 세션을 찾을 수 없습니다.");
    }
}
