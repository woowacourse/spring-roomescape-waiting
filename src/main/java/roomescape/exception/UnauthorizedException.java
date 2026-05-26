package roomescape.exception;

public class UnauthorizedException extends RoomescapeBaseException {
    public UnauthorizedException() {
        super("접근 권한이 없습니다. 관리자만 이용할 수 있습니다.");
    }
}
