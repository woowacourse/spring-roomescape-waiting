package roomescape.exception;

public class UnauthorizedException extends RoomescapeException {
    public UnauthorizedException() {
        super("UNMODIFIABLE", "예약을 조작하려면 사용자 이름이 반드시 필요합니다.");
    }
}
