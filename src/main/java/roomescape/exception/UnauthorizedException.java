package roomescape.exception;

public class UnauthorizedException extends IllegalArgumentException {
    public UnauthorizedException() {
        super("토큰이 없습니다.");
    }
}
